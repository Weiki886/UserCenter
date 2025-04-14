package com.weiki.usercenterbackend.aop;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.util.concurrent.RateLimiter;
import com.weiki.usercenterbackend.annotation.RateLimit;
import com.weiki.usercenterbackend.common.Fallback;
import com.weiki.usercenterbackend.config.RateLimitConfig;
import com.weiki.usercenterbackend.exception.RateLimitException;
import com.weiki.usercenterbackend.metrics.RateLimitMetrics;
import com.weiki.usercenterbackend.utils.JwtUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 限流切面
 * 基于AOP实现的三级限流策略
 */
@Aspect
@Component
@Order(1) // 确保限流在其他切面之前执行
public class RateLimiterAspect implements ApplicationContextAware {
    
    private static final Logger logger = LoggerFactory.getLogger(RateLimiterAspect.class);
    
    /**
     * 全局限流器
     */
    private RateLimiter globalRateLimiter;
    
    /**
     * 接口级别限流器缓存
     * key: 类名+方法名
     * value: RateLimiter实例
     */
    private LoadingCache<String, RateLimiter> interfaceLimiterCache;
    
    /**
     * 用户级别限流器缓存
     * key: 用户ID
     * value: RateLimiter实例
     */
    private LoadingCache<String, RateLimiter> userLimiterCache;
    
    // 限流器版本控制，用于解决ABA问题
    private static class VersionedRateLimiter {
        final RateLimiter rateLimiter;
        final long version;
        
        VersionedRateLimiter(RateLimiter rateLimiter, long version) {
            this.rateLimiter = rateLimiter;
            this.version = version;
        }
    }
    
    // 接口限流器的版本控制
    private final ConcurrentHashMap<String, AtomicReference<VersionedRateLimiter>> interfaceLimiterVersions = new ConcurrentHashMap<>();
    
    private final RateLimitConfig rateLimitConfig;
    private final RateLimitMetrics rateLimitMetrics;
    private final JwtUtils jwtUtils;
    private ApplicationContext applicationContext;
    
    @Autowired
    public RateLimiterAspect(RateLimitConfig rateLimitConfig, RateLimitMetrics rateLimitMetrics, JwtUtils jwtUtils) {
        this.rateLimitConfig = rateLimitConfig;
        this.rateLimitMetrics = rateLimitMetrics;
        this.jwtUtils = jwtUtils;
        initGlobalRateLimiter();
        initLimiterCaches();
    }
    
    /**
     * 初始化全局限流器
     */
    private void initGlobalRateLimiter() {
        double globalQps = rateLimitConfig.getGlobalQps();
        long warmupPeriod = rateLimitConfig.getGlobalWarmupPeriod();
        
        if (warmupPeriod > 0) {
            // 创建带预热的限流器
            globalRateLimiter = RateLimiter.create(globalQps, warmupPeriod, TimeUnit.SECONDS);
        } else {
            // 创建普通限流器
            globalRateLimiter = RateLimiter.create(globalQps);
        }
        
        logger.info("全局限流器初始化完成: QPS={}, 预热时间={}s", globalQps, warmupPeriod);
    }
    
    /**
     * 初始化限流器缓存
     */
    private void initLimiterCaches() {
        // 创建移除监听器，用于在缓存项被移除时上报指标
        RemovalListener<String, RateLimiter> removalListener = notification -> {
            String key = notification.getKey();
            RateLimiter limiter = notification.getValue();
            String[] parts = key.split(":", 2);
            String limiterType = parts.length > 1 ? parts[0] : "UNKNOWN";
            String limitTarget = parts.length > 1 ? parts[1] : key;
            
            // 移除时记录指标
            rateLimitMetrics.recordRemoval(limiterType, limitTarget, limiter.getRate());
            logger.debug("限流器已从缓存移除: type={}, target={}, reason={}", 
                    limiterType, limitTarget, notification.getCause());
        };
        
        // 接口级别限流器缓存
        interfaceLimiterCache = CacheBuilder.newBuilder()
                .expireAfterAccess(rateLimitConfig.getCacheExpireAfterAccess(), 
                        rateLimitConfig.getCacheExpireTimeUnit())
                .maximumSize(10000) // 设置最大缓存为10000个限流器
                .removalListener(removalListener)
                .recordStats()
                .build(new CacheLoader<String, RateLimiter>() {
                    @Override
                    public RateLimiter load(String key) {
                        // 默认使用注解上的配置，需要在调用时传入
                        return RateLimiter.create(5.0);
                    }
                });
        
        // 用户级别限流器缓存
        userLimiterCache = CacheBuilder.newBuilder()
                .expireAfterAccess(30, TimeUnit.MINUTES) // 30分钟未访问自动失效
                .maximumSize(10000) // 设置最大缓存为10000个限流器
                .removalListener(removalListener)
                .recordStats()
                .build(new CacheLoader<String, RateLimiter>() {
                    @Override
                    public RateLimiter load(String userId) {
                        // 所有用户默认使用相同的QPS限制
                        return RateLimiter.create(rateLimitConfig.getUserQps());
                    }
                });
        
        logger.info("限流器缓存初始化完成，过期时间: 30分钟，最大容量: 10000");
    }
    
    /**
     * 切面: 拦截所有带有@RateLimit注解的方法
     *
     * @param joinPoint 切点
     * @param rateLimit 注解实例
     * @return 方法执行结果
     * @throws Throwable 执行异常
     */
    @Around("@annotation(rateLimit)")
    public Object rateLimit(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        // 获取方法签名
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        String className = method.getDeclaringClass().getName();
        String methodName = method.getName();
        String fullMethodName = className + "." + methodName;
        
        // 记录开始时间
        long startTime = System.currentTimeMillis();
        
        // 获取限流类型
        RateLimit.LimitType limitType = rateLimit.limitType();
        boolean acquired = false;
        String limitTarget = fullMethodName;
        String limiterType;
        
        // 根据限流类型选择不同的限流策略
        switch (limitType) {
            case USER:
                // 用户级别限流
                String userId = getUserIdFromRequest();
                if (userId != null) {
                    limiterType = "USER";
                    limitTarget = userId;
                    acquired = acquireFromUserLimiter(userId, rateLimit);
                } else {
                    // 如果未登录，回退到接口级别限流
                    limiterType = "INTERFACE";
                    acquired = acquireFromInterfaceLimiter(fullMethodName, rateLimit);
                }
                break;
                
            case INTERFACE:
                // 接口级别限流
                limiterType = "INTERFACE";
                acquired = acquireFromInterfaceLimiter(fullMethodName, rateLimit);
                break;
                
            case DEFAULT:
            default:
                // 全局默认限流
                limiterType = "GLOBAL";
                acquired = acquireFromGlobalLimiter(rateLimit);
                break;
        }
        
        // 计算等待时间
        long waitTime = System.currentTimeMillis() - startTime;
        
        // 记录等待时间指标
        rateLimitMetrics.recordWaitTime(limiterType, limitTarget, waitTime);
        
        // 如果成功获取令牌，执行原方法
        if (acquired) {
            return joinPoint.proceed();
        }
        
        // 限流失败，尝试执行降级逻辑
        Class<?> fallbackClass = rateLimit.fallbackClass();
        if (fallbackClass != void.class) {
            try {
                Fallback fallback = (Fallback) applicationContext.getBean(fallbackClass);
                Fallback.RateLimitInfo rateLimitInfo = new Fallback.RateLimitInfo(
                        className, methodName, "限流触发", waitTime);
                return fallback.fallback(joinPoint, rateLimitInfo);
            } catch (BeansException e) {
                logger.error("降级处理失败，无法获取降级实现类: {}", fallbackClass.getName(), e);
            }
        }
        
        // 记录被拒绝的请求
        rateLimitMetrics.incrementRejectedCount(limiterType, limitTarget);
        
        // 抛出限流异常
        throw new RateLimitException(
                "请求频率超过限制",
                1000, // 建议1秒后重试
                limiterType,
                limitTarget
        );
    }
    
    /**
     * 从全局限流器获取令牌
     *
     * @param rateLimit 注解配置
     * @return 是否获取成功
     */
    private boolean acquireFromGlobalLimiter(RateLimit rateLimit) {
        double qps = globalRateLimiter.getRate();
        long timeout = rateLimit.timeout();
        TimeUnit timeoutUnit = rateLimit.timeoutUnit();
        
        // 每次更新可用令牌数指标
        rateLimitMetrics.updateAvailablePermits("GLOBAL", "GLOBAL", 
                globalRateLimiter.getRate() - globalRateLimiter.acquire(0));
        
        return acquireWithStrategy(globalRateLimiter, timeout, timeoutUnit);
    }
    
    /**
     * 从接口级别限流器获取令牌
     *
     * @param key 缓存键 (类名.方法名)
     * @param rateLimit 注解配置
     * @return 是否获取成功
     */
    private boolean acquireFromInterfaceLimiter(String key, RateLimit rateLimit) {
        try {
            // 加前缀，方便移除监听器区分限流器类型
            String cacheKey = "INTERFACE:" + key;
            
            // 获取或创建限流器
            RateLimiter limiter = getOrCreateInterfaceLimiter(cacheKey, rateLimit);
            
            // 更新指标
            rateLimitMetrics.updateAvailablePermits("INTERFACE", key, 
                    limiter.getRate() - limiter.acquire(0));
            
            // 根据配置选择获取令牌的策略
            return acquireWithStrategy(limiter, rateLimit.timeout(), rateLimit.timeoutUnit());
        } catch (ExecutionException e) {
            logger.error("获取接口限流器失败: {}", key, e);
            return false;
        }
    }
    
    /**
     * 从用户级别限流器获取令牌
     *
     * @param userId 用户ID
     * @param rateLimit 注解配置
     * @return 是否获取成功
     */
    private boolean acquireFromUserLimiter(String userId, RateLimit rateLimit) {
        try {
            // 加前缀，方便移除监听器区分限流器类型
            String cacheKey = "USER:" + userId;
            
            // 获取或创建用户限流器
            RateLimiter limiter = userLimiterCache.get(cacheKey);
            
            // 更新指标
            rateLimitMetrics.updateAvailablePermits("USER", userId, 
                    limiter.getRate() - limiter.acquire(0));
            
            // 根据配置选择获取令牌的策略
            return acquireWithStrategy(limiter, rateLimit.timeout(), rateLimit.timeoutUnit());
        } catch (ExecutionException e) {
            logger.error("获取用户限流器失败: {}", userId, e);
            return false;
        }
    }
    
    /**
     * 根据不同的策略从限流器获取令牌
     *
     * @param limiter 限流器实例
     * @param timeout 超时时间
     * @param timeoutUnit 超时时间单位
     * @return 是否获取成功
     */
    private boolean acquireWithStrategy(RateLimiter limiter, long timeout, TimeUnit timeoutUnit) {
        // 根据timeout参数选择不同的获取令牌策略
        if (timeout < 0) {
            // 完全阻塞式获取令牌，直到成功
            limiter.acquire();
            return true;
        } else if (timeout == 0) {
            // 非阻塞式获取令牌，立即返回结果
            return limiter.tryAcquire();
        } else {
            // 带超时的等待获取令牌
            return limiter.tryAcquire(timeout, timeoutUnit);
        }
    }
    
    /**
     * 获取或创建接口级别限流器
     * 使用AtomicReference+版本号解决ABA问题的双重检查锁实现
     *
     * @param key 缓存键 (类名.方法名)
     * @param rateLimit 注解配置
     * @return 限流器实例
     */
    private RateLimiter getOrCreateInterfaceLimiter(String key, RateLimit rateLimit) 
            throws ExecutionException {
        // 首先尝试从缓存获取
        RateLimiter limiter = interfaceLimiterCache.getIfPresent(key);
        
        if (limiter == null) {
            // 获取或创建版本控制器
            AtomicReference<VersionedRateLimiter> versionRef = interfaceLimiterVersions.computeIfAbsent(
                key, k -> new AtomicReference<>(null)
            );
            
            // 获取当前版本
            VersionedRateLimiter current = versionRef.get();
            long nextVersion = (current == null) ? 1 : current.version + 1;
            
            // 从注解获取配置
            double qps = rateLimit.qps();
            long warmupPeriod = rateLimit.warmupPeriod();
            TimeUnit warmupUnit = rateLimit.warmupUnit();
            
            // 创建新的限流器
            RateLimiter newLimiter;
            if (warmupPeriod > 0) {
                // 带预热期的限流器
                newLimiter = RateLimiter.create(qps, warmupPeriod, warmupUnit);
            } else {
                // 标准限流器
                newLimiter = RateLimiter.create(qps);
            }
            
            // 创建带版本的限流器
            VersionedRateLimiter newVersioned = new VersionedRateLimiter(newLimiter, nextVersion);
            
            // CAS更新，解决ABA问题
            if (versionRef.compareAndSet(current, newVersioned)) {
                // 放入缓存
                interfaceLimiterCache.put(key, newLimiter);
                limiter = newLimiter;
                
                logger.debug("创建接口限流器: {}, QPS={}, 预热时间={}, 版本={}",
                    key, qps, warmupPeriod > 0 ? warmupPeriod + " " + warmupUnit : "无", nextVersion);
            } else {
                // 有其他线程已经创建了限流器，尝试再次获取
                limiter = interfaceLimiterCache.get(key);
            }
        }
        
        return limiter;
    }
    
    /**
     * 从请求中获取用户ID
     *
     * @return 用户ID，未登录则返回null
     */
    private String getUserIdFromRequest() {
        ServletRequestAttributes attributes = 
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            // 从请求头获取JWT
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
                try {
                    // 从JWT中解析用户ID
                    Optional<String> userIdOpt = jwtUtils.getUserIdFromToken(token);
                    return userIdOpt.orElse(null);
                } catch (Exception e) {
                    logger.debug("从JWT解析用户ID失败", e);
                }
            }
        }
        
        return null;
    }
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
} 
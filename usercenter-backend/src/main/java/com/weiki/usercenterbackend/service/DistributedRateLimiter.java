package com.weiki.usercenterbackend.service;

import com.weiki.usercenterbackend.config.RateLimitConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 分布式限流器
 * 使用Redis Lua脚本实现的分布式限流，作为本地限流的补充
 */
@Service
public class DistributedRateLimiter {
    
    private static final Logger logger = LoggerFactory.getLogger(DistributedRateLimiter.class);
    
    /**
     * Redis Lua脚本
     */
    private RedisScript<Long> rateLimiterScript;
    
    /**
     * Redis键前缀
     */
    private static final String KEY_PREFIX = "rate_limiter:";
    
    /**
     * Redis令牌桶过期时间（秒）
     */
    private static final int BUCKET_EXPIRE_SECONDS = 10;
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final RateLimitConfig rateLimitConfig;
    
    @Autowired
    public DistributedRateLimiter(RedisTemplate<String, Object> redisTemplate, 
                                  RateLimitConfig rateLimitConfig) {
        this.redisTemplate = redisTemplate;
        this.rateLimitConfig = rateLimitConfig;
    }
    
    /**
     * 初始化Lua脚本
     */
    @PostConstruct
    public void init() {
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptSource(new ResourceScriptSource(
                new ClassPathResource(rateLimitConfig.getDistributedLimitLuaPath())));
        redisScript.setResultType(Long.class);
        this.rateLimiterScript = redisScript;
        logger.info("分布式限流器初始化完成，Lua脚本路径: {}", rateLimitConfig.getDistributedLimitLuaPath());
    }
    
    /**
     * 尝试获取令牌（非阻塞）
     *
     * @param key 限流键
     * @param maxPermits 最大令牌数（令牌桶容量）
     * @param rate 令牌产生速率（每秒）
     * @return 是否获取到令牌
     */
    public boolean tryAcquire(String key, double maxPermits, double rate) {
        return doTryAcquire(key, maxPermits, rate, 1, 0, TimeUnit.SECONDS);
    }
    
    /**
     * 尝试获取令牌，带超时等待
     *
     * @param key 限流键
     * @param maxPermits 最大令牌数（令牌桶容量）
     * @param rate 令牌产生速率（每秒）
     * @param timeout 超时时间
     * @param unit 时间单位
     * @return 是否获取到令牌
     */
    public boolean tryAcquire(String key, double maxPermits, double rate, long timeout, TimeUnit unit) {
        return doTryAcquire(key, maxPermits, rate, 1, timeout, unit);
    }
    
    /**
     * 尝试获取多个令牌
     *
     * @param key 限流键
     * @param maxPermits 最大令牌数（令牌桶容量）
     * @param rate 令牌产生速率（每秒）
     * @param permits 需要的令牌数
     * @param timeout 超时时间
     * @param unit 时间单位
     * @return 是否获取到令牌
     */
    private boolean doTryAcquire(String key, double maxPermits, double rate, int permits, 
                                long timeout, TimeUnit unit) {
        if (!rateLimitConfig.isDistributedLimitEnabled()) {
            // 未启用分布式限流，直接返回成功
            return true;
        }
        
        String redisKey = KEY_PREFIX + key;
        long now = System.currentTimeMillis();
        long ttl = unit.toMillis(timeout);
        long deadline = now + ttl;
        
        // 第一次尝试获取令牌
        Long result = executeLuaScript(redisKey, maxPermits, rate, permits, now);
        
        // 判断结果
        if (result == 1) {
            // 获取成功
            return true;
        } else if (ttl <= 0) {
            // 获取失败且不等待
            return false;
        }
        
        // 超时等待模式
        try {
            // 计算下次可以获取令牌的时间
            long nextAvailableTime = Math.max(now + 100, now + (1000 / (int) rate) * permits);
            
            while (now < deadline) {
                long sleepTime = Math.min(nextAvailableTime - now, deadline - now);
                if (sleepTime > 0) {
                    Thread.sleep(sleepTime);
                }
                
                now = System.currentTimeMillis();
                if (now >= deadline) {
                    break;
                }
                
                // 再次尝试获取令牌
                result = executeLuaScript(redisKey, maxPermits, rate, permits, now);
                if (result == 1) {
                    return true;
                }
                
                // 更新下次尝试时间
                nextAvailableTime = now + (1000 / (int) rate) * permits;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("等待获取令牌被中断", e);
        }
        
        return false;
    }
    
    /**
     * 执行Redis Lua脚本
     *
     * @param key Redis键
     * @param maxPermits 最大令牌数
     * @param rate 令牌产生速率
     * @param permits 需要的令牌数
     * @param now 当前时间戳
     * @return 执行结果：1=成功，0=失败
     */
    private Long executeLuaScript(String key, double maxPermits, double rate, int permits, long now) {
        List<String> keys = Collections.singletonList(key);
        return redisTemplate.execute(rateLimiterScript, keys, 
                maxPermits, rate, permits, now, BUCKET_EXPIRE_SECONDS);
    }
} 
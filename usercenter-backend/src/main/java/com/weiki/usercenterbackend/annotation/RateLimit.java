package com.weiki.usercenterbackend.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * 接口限流注解
 * 可配置QPS、预热时间和获取令牌超时时间
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {
    
    /**
     * 每秒允许的请求次数（QPS）
     * @return QPS值
     */
    double qps() default 5.0;
    
    /**
     * 预热时间（稳定限流前的过渡期）
     * 在预热期间，RateLimiter会逐步增加放行请求的速率，直到达到目标QPS
     * @return 预热时间
     */
    long warmupPeriod() default 0;
    
    /**
     * 预热时间单位
     * @return 时间单位
     */
    TimeUnit warmupUnit() default TimeUnit.SECONDS;
    
    /**
     * 获取令牌的最大等待时间，超过则限流失败
     * 当值为0时表示非阻塞模式（tryAcquire）
     * 当值为-1时表示完全阻塞模式（acquire）
     * 当值大于0时表示带超时的等待模式（tryAcquire with timeout）
     * @return 等待超时时间
     */
    long timeout() default 0;
    
    /**
     * 超时时间单位
     * @return 时间单位
     */
    TimeUnit timeoutUnit() default TimeUnit.MILLISECONDS;
    
    /**
     * 限流的类型，可以是基于全局、接口或用户的限流
     * @return 限流类型
     */
    LimitType limitType() default LimitType.DEFAULT;
    
    /**
     * 限流类型的枚举
     */
    enum LimitType {
        /**
         * 全局默认限流
         */
        DEFAULT,
        
        /**
         * 接口级别的限流
         */
        INTERFACE,
        
        /**
         * 基于用户的限流
         */
        USER
    }
    
    /**
     * 限流被触发时的降级接口实现类
     * 降级接口需实现Fallback接口
     * @return 降级实现类
     */
    Class<?> fallbackClass() default void.class;
    
    /**
     * 限流优先级，数值越高优先级越高
     * 在多级限流同时触发的情况下，高优先级接口可获得更多资源
     * @return 优先级权重(1-10)
     */
    int priority() default 5;
} 
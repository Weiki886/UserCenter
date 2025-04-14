package com.weiki.usercenterbackend.exception;

/**
 * 限流异常
 * 当接口被限流时抛出此异常
 */
public class RateLimitException extends RuntimeException {
    
    /**
     * 重试等待时间（毫秒）
     */
    private final long retryAfterMs;
    
    /**
     * 限流器类型
     */
    private final String limiterType;
    
    /**
     * 限流目标（接口/用户）
     */
    private final String limitTarget;
    
    /**
     * 创建一个限流异常
     *
     * @param message 异常消息
     * @param retryAfterMs 建议重试等待时间（毫秒）
     * @param limiterType 限流器类型
     * @param limitTarget 限流目标
     */
    public RateLimitException(String message, long retryAfterMs, String limiterType, String limitTarget) {
        super(message);
        this.retryAfterMs = retryAfterMs;
        this.limiterType = limiterType;
        this.limitTarget = limitTarget;
    }
    
    /**
     * 获取建议重试等待时间（毫秒）
     *
     * @return 重试等待时间
     */
    public long getRetryAfterMs() {
        return retryAfterMs;
    }
    
    /**
     * 获取建议重试等待时间（秒）
     * 主要用于HTTP响应头Retry-After
     *
     * @return 重试等待时间（秒）
     */
    public long getRetryAfterSeconds() {
        return (long) Math.ceil(retryAfterMs / 1000.0);
    }
    
    /**
     * 获取限流器类型
     *
     * @return 限流器类型
     */
    public String getLimiterType() {
        return limiterType;
    }
    
    /**
     * 获取限流目标
     *
     * @return 限流目标
     */
    public String getLimitTarget() {
        return limitTarget;
    }
} 
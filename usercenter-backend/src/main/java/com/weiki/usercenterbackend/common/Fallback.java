package com.weiki.usercenterbackend.common;

import org.aspectj.lang.ProceedingJoinPoint;

/**
 * 限流降级接口
 * 实现此接口可提供被限流时的降级策略
 */
public interface Fallback {
    
    /**
     * 在被限流时执行的降级方法
     *
     * @param joinPoint 切点信息，包含原始方法和参数
     * @param rateLimitInfo 限流相关信息，如触发原因、等待时间等
     * @return 降级返回结果
     */
    Object fallback(ProceedingJoinPoint joinPoint, RateLimitInfo rateLimitInfo);
    
    /**
     * 限流信息记录类，包含限流的详细信息
     */
    class RateLimitInfo {
        private String method;
        private String className;
        private String reason;
        private long waitTimeMillis;
        private long timestamp;
        
        public RateLimitInfo(String className, String method, String reason, long waitTimeMillis) {
            this.className = className;
            this.method = method;
            this.reason = reason;
            this.waitTimeMillis = waitTimeMillis;
            this.timestamp = System.currentTimeMillis();
        }
        
        public String getMethod() {
            return method;
        }
        
        public String getClassName() {
            return className;
        }
        
        public String getReason() {
            return reason;
        }
        
        public long getWaitTimeMillis() {
            return waitTimeMillis;
        }
        
        public long getTimestamp() {
            return timestamp;
        }
    }
} 
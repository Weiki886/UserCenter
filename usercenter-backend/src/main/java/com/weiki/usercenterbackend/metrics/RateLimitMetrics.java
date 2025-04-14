package com.weiki.usercenterbackend.metrics;

import io.micrometer.core.instrument.*;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 限流器监控指标
 * 用于通过Prometheus监控限流器运行情况
 */
@Component
public class RateLimitMetrics {
    
    private final MeterRegistry meterRegistry;
    
    /**
     * 缓存已创建的计量器，避免重复创建
     */
    private final ConcurrentHashMap<String, Timer> waitTimeTimers = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Counter> rejectedCounters = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Gauge> availablePermitsGauges = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Counter> removedCounters = new ConcurrentHashMap<>();
    
    /**
     * 构造函数，注入MeterRegistry
     *
     * @param meterRegistry Micrometer注册表
     */
    public RateLimitMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }
    
    /**
     * 记录限流器等待时间
     *
     * @param limiterType 限流器类型
     * @param limitTarget 限流目标（接口/用户）
     * @param waitTimeMs 等待时间（毫秒）
     */
    public void recordWaitTime(String limiterType, String limitTarget, long waitTimeMs) {
        Timer timer = waitTimeTimers.computeIfAbsent(
            getMetricKey(limiterType, limitTarget),
            key -> Timer.builder("http_requests_limiter_wait_time")
                .tag("type", limiterType)
                .tag("target", limitTarget)
                .description("API请求获取令牌等待时间")
                .register(meterRegistry)
        );
        
        timer.record(waitTimeMs, TimeUnit.MILLISECONDS);
    }
    
    /**
     * 记录被拒绝的请求数
     *
     * @param limiterType 限流器类型
     * @param limitTarget 限流目标（接口/用户）
     */
    public void incrementRejectedCount(String limiterType, String limitTarget) {
        Counter counter = rejectedCounters.computeIfAbsent(
            getMetricKey(limiterType, limitTarget),
            key -> Counter.builder("http_requests_limiter_rejected_total")
                .tag("type", limiterType)
                .tag("target", limitTarget)
                .description("API请求被限流拒绝总数")
                .register(meterRegistry)
        );
        
        counter.increment();
    }
    
    /**
     * 记录限流器从缓存中移除的事件
     *
     * @param limiterType 限流器类型
     * @param limitTarget 限流目标（接口/用户）
     * @param rate 限流器的速率
     */
    public void recordRemoval(String limiterType, String limitTarget, double rate) {
        Counter counter = removedCounters.computeIfAbsent(
            getMetricKey(limiterType, limitTarget),
            key -> Counter.builder("http_requests_limiter_removed_total")
                .tag("type", limiterType)
                .tag("target", limitTarget)
                .tag("rate", String.valueOf(rate))
                .description("限流器从缓存中移除的总数")
                .register(meterRegistry)
        );
        
        counter.increment();
        
        // 移除相关的其他指标
        String key = getMetricKey(limiterType, limitTarget);
        availablePermitsGauges.remove(key);
    }
    
    /**
     * 更新可用令牌数量
     *
     * @param limiterType 限流器类型
     * @param limitTarget 限流目标（接口/用户）
     * @param availablePermits 可用令牌数
     */
    public void updateAvailablePermits(String limiterType, String limitTarget, Number availablePermits) {
        String key = getMetricKey(limiterType, limitTarget);
        
        // 如果已有此Gauge则更新，否则创建新的
        if (!availablePermitsGauges.containsKey(key)) {
            Gauge gauge = Gauge.builder("http_requests_limiter_available_permits", 
                    () -> availablePermits.doubleValue())
                .tag("type", limiterType)
                .tag("target", limitTarget)
                .description("API请求限流器可用令牌数")
                .register(meterRegistry);
            
            availablePermitsGauges.put(key, gauge);
        }
    }
    
    /**
     * 生成指标唯一键
     *
     * @param limiterType 限流器类型
     * @param limitTarget 限流目标
     * @return 唯一键
     */
    private String getMetricKey(String limiterType, String limitTarget) {
        return limiterType + ":" + limitTarget;
    }
} 
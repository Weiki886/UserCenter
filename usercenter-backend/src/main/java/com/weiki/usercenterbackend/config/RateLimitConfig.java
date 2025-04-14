package com.weiki.usercenterbackend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * 限流配置类
 * 从配置文件中读取全局限流配置
 */
@Configuration
public class RateLimitConfig {

    /**
     * 全局默认限流QPS
     */
    @Value("${rate.limit.global.qps:10.0}")
    private double globalQps;
    
    /**
     * 全局默认预热时间（秒）
     */
    @Value("${rate.limit.global.warmup:0}")
    private long globalWarmupPeriod;
    
    /**
     * 全局默认超时时间（毫秒）
     * 默认0表示非阻塞模式
     */
    @Value("${rate.limit.global.timeout:0}")
    private long globalTimeout;
    
    /**
     * 用户级别限流的默认QPS
     */
    @Value("${rate.limit.user.qps:5.0}")
    private double userQps;
    
    /**
     * 限流器缓存的过期时间（分钟）
     */
    @Value("${rate.limit.cache.expire:30}")
    private long cacheExpireAfterAccess;
    
    /**
     * 限流器缓存的最大数量
     */
    @Value("${rate.limit.cache.maximum.size:1000}")
    private long cacheMaximumSize;
    
    /**
     * 是否启用分布式限流
     */
    @Value("${rate.limit.distributed.enabled:false}")
    private boolean distributedLimitEnabled;
    
    /**
     * Redis分布式限流脚本路径
     */
    @Value("${rate.limit.distributed.lua.path:classpath:scripts/rate_limiter.lua}")
    private String distributedLimitLuaPath;
    
    /**
     * 令牌桶大小倍率，用于突发流量处理
     * 例如：倍率为2，QPS为10，则桶容量为20
     */
    @Value("${rate.limit.burst.factor:2.0}")
    private double burstFactor;
    
    /**
     * 获取全局默认QPS
     */
    public double getGlobalQps() {
        return globalQps;
    }
    
    /**
     * 获取全局默认预热时间
     */
    public long getGlobalWarmupPeriod() {
        return globalWarmupPeriod;
    }
    
    /**
     * 获取全局默认超时时间
     */
    public long getGlobalTimeout() {
        return globalTimeout;
    }
    
    /**
     * 获取用户级别默认QPS
     */
    public double getUserQps() {
        return userQps;
    }
    
    /**
     * 获取限流器缓存的过期时间
     */
    public long getCacheExpireAfterAccess() {
        return cacheExpireAfterAccess;
    }
    
    /**
     * 获取限流器缓存的最大数量
     */
    public long getCacheMaximumSize() {
        return cacheMaximumSize;
    }
    
    /**
     * 是否启用分布式限流
     */
    public boolean isDistributedLimitEnabled() {
        return distributedLimitEnabled;
    }
    
    /**
     * 获取分布式限流Lua脚本路径
     */
    public String getDistributedLimitLuaPath() {
        return distributedLimitLuaPath;
    }
    
    /**
     * 获取令牌桶大小倍率
     */
    public double getBurstFactor() {
        return burstFactor;
    }
    
    /**
     * 获取缓存过期的时间单位
     */
    public TimeUnit getCacheExpireTimeUnit() {
        return TimeUnit.MINUTES;
    }
} 
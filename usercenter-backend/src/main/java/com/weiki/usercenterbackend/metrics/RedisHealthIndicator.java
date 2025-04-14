package com.weiki.usercenterbackend.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.PostConstruct;
import java.util.Properties;

/**
 * Redis健康指标监控器
 * 用于监控Redis连接状态和性能指标
 */
@Slf4j
@Configuration
public class RedisHealthIndicator extends AbstractHealthIndicator {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Autowired
    private MeterRegistry meterRegistry;
    
    private Counter redisErrorCounter;
    private Counter redisHitCounter;
    private Counter redisMissCounter;
    private Timer redisOperationTimer;
    
    @PostConstruct
    public void init() {
        // 初始化指标计数器
        redisErrorCounter = Counter.builder("redis.errors")
            .description("Redis操作错误计数")
            .register(meterRegistry);
            
        redisHitCounter = Counter.builder("redis.cache.hits")
            .description("Redis缓存命中计数")
            .register(meterRegistry);
            
        redisMissCounter = Counter.builder("redis.cache.misses")
            .description("Redis缓存未命中计数")
            .register(meterRegistry);
            
        redisOperationTimer = Timer.builder("redis.operation.time")
            .description("Redis操作执行时间")
            .register(meterRegistry);
    }
    
    @Override
    protected void doHealthCheck(Health.Builder builder) {
        try {
            // 执行简单的Redis操作，检查连接性
            Timer.Sample sample = Timer.start();
            String result = redisTemplate.execute((RedisCallback<String>) connection -> {
                try {
                    return new String(connection.ping());
                } catch (Exception e) {
                    log.error("Redis ping失败: {}", e.getMessage());
                    return null;
                }
            });
            sample.stop(redisOperationTimer);
            
            if ("PONG".equalsIgnoreCase(result)) {
                // Redis正常运行
                builder.up()
                    .withDetail("version", getRedisVersion())
                    .withDetail("clientCount", getClientCount())
                    .withDetail("memoryUsage", getMemoryInfo())
                    .withDetail("uptime", getUptime());
            } else {
                // Redis连接异常
                builder.down()
                    .withDetail("ping", result != null ? result : "failed");
                redisErrorCounter.increment();
            }
        } catch (Exception e) {
            // 捕获异常，记录监控指标
            log.error("Redis健康检查异常: {}", e.getMessage(), e);
            redisErrorCounter.increment();
            builder.down(e);
        }
    }
    
    /**
     * 获取Redis版本信息
     */
    private String getRedisVersion() {
        try {
            return redisTemplate.execute((RedisCallback<String>) connection -> {
                Properties props = connection.info();
                return props.getProperty("redis_version", "unknown");
            });
        } catch (Exception e) {
            log.error("获取Redis版本失败: {}", e.getMessage());
            return "unknown";
        }
    }
    
    /**
     * 获取Redis客户端连接数
     */
    private int getClientCount() {
        try {
            return redisTemplate.execute((RedisCallback<Integer>) connection -> {
                Properties props = connection.info("clients");
                String connectedClients = props.getProperty("connected_clients", "0");
                return Integer.parseInt(connectedClients);
            });
        } catch (Exception e) {
            log.error("获取Redis客户端连接数失败: {}", e.getMessage());
            return -1;
        }
    }
    
    /**
     * 获取Redis内存使用信息
     */
    private String getMemoryInfo() {
        try {
            return redisTemplate.execute((RedisCallback<String>) connection -> {
                Properties props = connection.info("memory");
                return props.getProperty("used_memory_human", "unknown");
            });
        } catch (Exception e) {
            log.error("获取Redis内存使用信息失败: {}", e.getMessage());
            return "unknown";
        }
    }
    
    /**
     * 获取Redis运行时间
     */
    private String getUptime() {
        try {
            return redisTemplate.execute((RedisCallback<String>) connection -> {
                Properties props = connection.info("server");
                String uptimeInSeconds = props.getProperty("uptime_in_seconds", "0");
                int uptime = Integer.parseInt(uptimeInSeconds);
                
                // 转换为可读格式
                int days = uptime / (24 * 3600);
                int hours = (uptime % (24 * 3600)) / 3600;
                int minutes = (uptime % 3600) / 60;
                int seconds = uptime % 60;
                
                return String.format("%d天%d小时%d分%d秒", days, hours, minutes, seconds);
            });
        } catch (Exception e) {
            log.error("获取Redis运行时间失败: {}", e.getMessage());
            return "unknown";
        }
    }
    
    /**
     * 增加缓存命中计数
     */
    public void incrementHitCount() {
        redisHitCounter.increment();
    }
    
    /**
     * 增加缓存未命中计数
     */
    public void incrementMissCount() {
        redisMissCounter.increment();
    }
    
    /**
     * 增加错误计数
     */
    public void incrementErrorCount() {
        redisErrorCounter.increment();
    }
} 
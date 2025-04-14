package com.weiki.usercenterbackend.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 指标监控配置
 */
@Configuration
public class MetricsConfig {

    /**
     * 自定义Meter Registry配置
     *
     * @return MeterRegistryCustomizer
     */
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> registry.config()
                .commonTags("application", "usercenter")
                .commonTags("environment", "production");
    }

    /**
     * 系统状态指标
     *
     * @return MeterBinder
     */
    @Bean
    public MeterBinder systemStatusMetrics() {
        return registry -> {
            // 系统状态指标（0=维护中，1=正常）
            AtomicInteger systemStatus = new AtomicInteger(1);
            
            Gauge.builder("system.status", systemStatus, AtomicInteger::get)
                    .description("System operational status (0=maintenance, 1=operational)")
                    .tag("type", "availability")
                    .register(registry);
        };
    }

    /**
     * 活跃用户数指标
     *
     * @return MeterBinder
     */
    @Bean
    public MeterBinder activeUsersMetrics() {
        return registry -> {
            // 活跃用户数指标（模拟值）
            AtomicInteger activeUsers = new AtomicInteger(100);
            
            Gauge.builder("users.active", activeUsers, AtomicInteger::get)
                    .description("Number of currently active users")
                    .tag("type", "user")
                    .register(registry);
            
            // 定期更新活跃用户数（实际应用中应通过真实数据更新）
            Thread updateThread = new Thread(() -> {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        Thread.sleep(10000);
                        // 模拟波动
                        activeUsers.set(100 + (int) (Math.random() * 50));
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            });
            updateThread.setDaemon(true);
            updateThread.start();
        };
    }

    /**
     * 应用服务计时器
     *
     * @param registry MeterRegistry
     * @return Timer
     */
    @Bean
    public Timer applicationServiceTimer(MeterRegistry registry) {
        return Timer.builder("application.service.time")
                .description("Time taken by application service calls")
                .tag("service", "user-service")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(registry);
    }
} 
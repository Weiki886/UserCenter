package com.weiki.usercenterbackend.ratelimit;

import com.google.common.util.concurrent.RateLimiter;
import com.weiki.usercenterbackend.metrics.RateLimitMetrics;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 限流系统测试类
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
public class RateLimiterTest {
    
    @MockBean
    private RateLimitMetrics rateLimitMetrics;
    
    /**
     * 测试Guava RateLimiter的基本功能
     */
    @Test
    public void testBasicRateLimiter() {
        // 创建一个QPS为5的限流器
        RateLimiter limiter = RateLimiter.create(5.0);
        
        // 测试非阻塞模式
        boolean firstAcquired = limiter.tryAcquire();
        assertTrue(firstAcquired, "首次获取令牌应该成功");
        
        // 一次性尝试获取6个令牌，应该失败
        boolean batchAcquired = limiter.tryAcquire(6);
        assertFalse(batchAcquired, "尝试获取超过QPS的令牌数应该失败");
        
        // 测试超时等待模式
        long startTime = System.currentTimeMillis();
        boolean timeoutAcquired = limiter.tryAcquire(2, 1, TimeUnit.SECONDS);
        long endTime = System.currentTimeMillis();
        
        // 验证等待时间在预期范围内 (200ms-1000ms)
        long waitTime = endTime - startTime;
        assertTrue(waitTime >= 200 && waitTime <= 1000, 
                "等待时间应该在预期范围内，实际为: " + waitTime + "ms");
    }
    
    /**
     * 测试限流器的预热功能
     */
    @Test
    public void testWarmupRateLimiter() {
        // 创建一个带3秒预热期的限流器，目标QPS为10
        RateLimiter limiter = RateLimiter.create(10.0, 3, TimeUnit.SECONDS);
        
        // 记录前5次请求的等待时间
        List<Double> waitTimes = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            waitTimes.add(limiter.acquire());
            try {
                // 短暂等待，确保能观察到斜率
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        // 验证预热期内，等待时间逐渐减少（限流逐渐放宽）
        for (int i = 1; i < waitTimes.size(); i++) {
            assertTrue(waitTimes.get(i) <= waitTimes.get(i-1) * 1.1, 
                    "预热期内，等待时间应该逐渐减少，或至少不会显著增加");
        }
    }
    
    /**
     * 测试限流器在高并发场景下的表现
     */
    @Test
    public void testConcurrentRateLimiter() throws Exception {
        // 创建一个QPS为10的限流器
        final RateLimiter limiter = RateLimiter.create(10.0);
        
        // 创建20个线程的线程池
        ExecutorService executor = Executors.newFixedThreadPool(20);
        
        // 记录成功获取令牌的次数
        final AtomicInteger successCount = new AtomicInteger(0);
        
        // 在1秒内尝试执行50个任务
        for (int i = 0; i < 50; i++) {
            executor.submit(() -> {
                if (limiter.tryAcquire(100, TimeUnit.MILLISECONDS)) {
                    successCount.incrementAndGet();
                }
            });
        }
        
        // 关闭线程池并等待所有任务完成
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);
        
        // 验证成功次数在预期范围内 (10-15次，考虑到执行时间)
        int actualSuccess = successCount.get();
        assertTrue(actualSuccess >= 10 && actualSuccess <= 15, 
                "成功获取令牌的次数应该在预期范围内，实际为: " + actualSuccess);
    }
    
    /**
     * 使用Awaitility测试库验证限流器的精确性
     */
    @Test
    public void testRateLimiterPrecision() {
        // 创建一个QPS为5的限流器
        final RateLimiter limiter = RateLimiter.create(5.0);
        
        // 记录成功获取的令牌数
        final AtomicInteger acquiredTokens = new AtomicInteger(0);
        
        // 初始化成功：立即获取一个令牌
        limiter.acquire();
        acquiredTokens.incrementAndGet();
        
        // 使用Awaitility验证2秒后能够获取到额外的10个令牌
        Awaitility.await()
                .atMost(3, TimeUnit.SECONDS)
                .pollInterval(100, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> {
                    for (int i = 0; i < 10; i++) {
                        if (limiter.tryAcquire()) {
                            acquiredTokens.incrementAndGet();
                        }
                    }
                    // 在2秒内，能获取的令牌应该接近10个
                    assertTrue(acquiredTokens.get() >= 10, 
                            "2秒内应该能获取大约10个令牌，实际获取: " + acquiredTokens.get());
                });
    }
} 
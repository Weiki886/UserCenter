package com.weiki.usercenterbackend.service.impl;

import com.weiki.usercenterbackend.service.DistributedLockService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Redis分布式锁实现
 */
@Service
@Slf4j
@ConditionalOnProperty(name = "spring.redis.enabled", havingValue = "true")
public class DistributedLockServiceImpl implements DistributedLockService {

    @Autowired
    private RedissonClient redissonClient;
    
    @Autowired(required = false)
    private MeterRegistry meterRegistry;
    
    // 锁缓存，减少重复创建锁对象的开销
    private final ConcurrentHashMap<String, RLock> lockMap = new ConcurrentHashMap<>();
    
    // 锁计数器
    private final ConcurrentHashMap<String, Integer> lockCounter = new ConcurrentHashMap<>();
    
    // 监控指标
    private Counter lockAcquireSuccessCounter;
    private Counter lockAcquireFailureCounter;
    private Timer lockWaitTimer;
    private Counter lockReleaseCounter;
    
    @PostConstruct
    public void init() {
        if (meterRegistry != null) {
            // 初始化监控指标
            lockAcquireSuccessCounter = Counter.builder("lock.acquire.success")
                .description("分布式锁获取成功次数")
                .register(meterRegistry);
                
            lockAcquireFailureCounter = Counter.builder("lock.acquire.failure")
                .description("分布式锁获取失败次数")
                .register(meterRegistry);
                
            lockWaitTimer = Timer.builder("lock.wait.time")
                .description("分布式锁等待时间")
                .register(meterRegistry);
                
            lockReleaseCounter = Counter.builder("lock.release")
                .description("分布式锁释放次数")
                .register(meterRegistry);
        }
    }

    @Override
    public boolean tryLock(String lockKey, long waitTime, long leaseTime, TimeUnit timeUnit, boolean isFair) {
        if (redissonClient == null) {
            log.error("Redisson客户端未初始化，无法使用分布式锁");
            if (lockAcquireFailureCounter != null) {
                lockAcquireFailureCounter.increment();
            }
            return false;
        }
        
        try {
            // 使用双重检查模式获取或创建锁对象
            RLock lock = lockMap.get(lockKey);
            if (lock == null) {
                synchronized (this) {
                    lock = lockMap.get(lockKey);
                    if (lock == null) {
                        lock = isFair ? redissonClient.getFairLock(lockKey) : redissonClient.getLock(lockKey);
                        lockMap.put(lockKey, lock);
                        log.debug("创建新的分布式锁对象: {}", lockKey);
                    }
                }
            }
            
            // 使用计时器记录锁等待时间
            Timer.Sample sample = null;
            if (lockWaitTimer != null) {
                sample = Timer.start();
            }
            
            // 尝试获取锁
            boolean acquired = lock.tryLock(waitTime, leaseTime, timeUnit);
            
            // 记录锁获取结果
            if (acquired) {
                if (lockAcquireSuccessCounter != null) {
                    lockAcquireSuccessCounter.increment();
                }
                // 增加锁计数
                lockCounter.compute(lockKey, (key, count) -> count == null ? 1 : count + 1);
                log.debug("获取分布式锁成功: {}", lockKey);
            } else {
                if (lockAcquireFailureCounter != null) {
                    lockAcquireFailureCounter.increment();
                }
                log.warn("获取分布式锁失败: {}, 等待时间: {}ms", lockKey, timeUnit.toMillis(waitTime));
            }
            
            // 记录等待时间
            if (sample != null && lockWaitTimer != null) {
                sample.stop(lockWaitTimer);
            }
            
            return acquired;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("获取分布式锁被中断: {}", lockKey, e);
            if (lockAcquireFailureCounter != null) {
                lockAcquireFailureCounter.increment();
            }
            return false;
        } catch (Exception e) {
            log.error("获取分布式锁异常: {}", lockKey, e);
            if (lockAcquireFailureCounter != null) {
                lockAcquireFailureCounter.increment();
            }
            return false;
        }
    }

    @Override
    public void unlock(String lockKey) {
        RLock lock = lockMap.get(lockKey);
        if (lock != null && lock.isHeldByCurrentThread()) {
            try {
                // 减少锁计数
                lockCounter.compute(lockKey, (key, count) -> {
                    if (count == null || count <= 1) {
                        return null; // 移除计数
                    }
                    return count - 1;
                });
                
                // 释放锁
                lock.unlock();
                
                if (lockReleaseCounter != null) {
                    lockReleaseCounter.increment();
                }
                log.debug("释放分布式锁成功: {}", lockKey);
            } catch (Exception e) {
                log.error("释放分布式锁异常: {}", lockKey, e);
            }
        } else {
            log.warn("尝试释放未持有的分布式锁: {}", lockKey);
        }
    }
    
    /**
     * 获取当前锁的使用数量
     * 
     * @return 锁数量
     */
    public int getLockCount() {
        return lockMap.size();
    }
    
    /**
     * 获取指定锁的使用次数
     * 
     * @param lockKey 锁键
     * @return 使用次数
     */
    public int getLockUseCount(String lockKey) {
        return lockCounter.getOrDefault(lockKey, 0);
    }
    
    /**
     * 判断锁是否被当前线程持有
     * 
     * @param lockKey 锁键
     * @return 是否持有
     */
    public boolean isLocked(String lockKey) {
        RLock lock = lockMap.get(lockKey);
        return lock != null && lock.isLocked();
    }
    
    /**
     * 获取锁的剩余租期，单位毫秒
     * 
     * @param lockKey 锁键
     * @return 剩余租期，-1表示锁不存在或没有获取到
     */
    public long getLockRemainingLeaseTime(String lockKey) {
        RLock lock = lockMap.get(lockKey);
        return lock != null ? lock.remainTimeToLive() : -1;
    }
    
    /**
     * 强制释放锁（谨慎使用）
     * 
     * @param lockKey 锁键
     * @return 是否成功
     */
    public void forceUnlock(String lockKey) {
        RLock lock = lockMap.get(lockKey);
        if (lock != null) {
            try {
                boolean result = lock.forceUnlock();
                if (result) {
                    lockCounter.remove(lockKey);
                    log.warn("强制释放分布式锁: {}", lockKey);
                }
            } catch (Exception e) {
                log.error("强制释放分布式锁异常: {}", lockKey, e);
            }
        } else {
            log.warn("尝试释放不存在的分布式锁: {}", lockKey);
        }
    }
} 
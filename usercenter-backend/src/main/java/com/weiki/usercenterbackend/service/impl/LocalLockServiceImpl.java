package com.weiki.usercenterbackend.service.impl;

import com.weiki.usercenterbackend.service.DistributedLockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 基于本地JVM的锁服务实现
 * 当Redis不可用或未启用时，使用本地锁作为替代
 */
@Service
@Slf4j
@Primary // 标记为主要实现，当有多个实现时优先使用这个
@ConditionalOnProperty(name = "spring.redis.enabled", havingValue = "false", matchIfMissing = true)
public class LocalLockServiceImpl implements DistributedLockService {

    /**
     * 本地锁存储，使用ConcurrentHashMap保证线程安全
     */
    private final Map<String, LockInfo> localLocks = new ConcurrentHashMap<>();

    @Override
    public boolean tryLock(String key, long waitTime, long leaseTime, TimeUnit unit, boolean isFair) {
        log.info("使用本地锁模式，尝试获取锁: {}", key);
        
        try {
            // 获取或创建锁
            LockInfo lockInfo = localLocks.computeIfAbsent(key, k -> new LockInfo(new ReentrantLock(isFair)));
            Lock lock = lockInfo.getLock();
            
            // 尝试获取锁
            boolean acquired = lock.tryLock(waitTime, unit);
            if (acquired) {
                // 记录持有锁的线程ID
                lockInfo.setHoldingThreadId(Thread.currentThread().getId());
                log.debug("获取本地锁成功: {}", key);
                
                // 设置过期时间（如果有）
                if (leaseTime > 0) {
                    scheduleUnlock(key, leaseTime, unit);
                }
            } else {
                log.warn("获取本地锁失败: {}", key);
            }
            
            return acquired;
        } catch (InterruptedException e) {
            log.error("获取本地锁时被中断: {}", key, e);
            Thread.currentThread().interrupt();
            return false;
        } catch (Exception e) {
            log.error("获取本地锁异常: {}", key, e);
            return false;
        }
    }

    @Override
    public void unlock(String key) {
        log.debug("释放本地锁: {}", key);
        LockInfo lockInfo = localLocks.get(key);
        if (lockInfo != null && lockInfo.isHeldByCurrentThread()) {
            try {
                Lock lock = lockInfo.getLock();
                lockInfo.setHoldingThreadId(-1); // 清除持有线程ID
                lock.unlock();
                log.debug("本地锁已释放: {}", key);
            } catch (Exception e) {
                log.error("释放本地锁异常: {}", key, e);
            }
        }
    }

    @Override
    public void forceUnlock(String key) {
        log.warn("强制释放本地锁: {}", key);
        localLocks.remove(key);
        log.debug("本地锁已强制移除: {}", key);
    }

    /**
     * 设置锁的自动释放
     */
    private void scheduleUnlock(String key, long leaseTime, TimeUnit unit) {
        // 创建一个线程在指定时间后释放锁
        Thread unlockThread = new Thread(() -> {
            try {
                unit.sleep(leaseTime);
                forceUnlock(key);
                log.debug("本地锁自动过期释放: {}", key);
            } catch (InterruptedException e) {
                log.error("本地锁自动释放被中断: {}", key, e);
                Thread.currentThread().interrupt();
            }
        });
        unlockThread.setDaemon(true);
        unlockThread.start();
    }

    /**
     * 锁信息内部类，包装Lock实例并记录持有者线程ID
     */
    private static class LockInfo {
        private final Lock lock;
        private volatile long holdingThreadId = -1;

        public LockInfo(Lock lock) {
            this.lock = lock;
        }

        public Lock getLock() {
            return lock;
        }

        public boolean isHeldByCurrentThread() {
            return holdingThreadId == Thread.currentThread().getId();
        }

        public void setHoldingThreadId(long threadId) {
            this.holdingThreadId = threadId;
        }
    }
} 
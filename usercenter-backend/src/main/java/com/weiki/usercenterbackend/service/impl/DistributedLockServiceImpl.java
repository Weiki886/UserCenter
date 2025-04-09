package com.weiki.usercenterbackend.service.impl;

import com.weiki.usercenterbackend.service.DistributedLockService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * 基于Redisson的分布式锁服务实现
 */
@Service
@Slf4j
@ConditionalOnBean(RedissonClient.class)
@ConditionalOnProperty(name = "spring.redis.enabled", havingValue = "true")
public class DistributedLockServiceImpl implements DistributedLockService {

    @Resource
    private RedissonClient redissonClient;

    @Override
    public boolean tryLock(String key, long waitTime, long leaseTime, TimeUnit unit, boolean isFair) {
        try {
            RLock lock = getLock(key, isFair);
            log.debug("尝试获取分布式锁: {}", key);
            return lock.tryLock(waitTime, leaseTime, unit);
        } catch (InterruptedException e) {
            log.error("获取锁时被中断: {}", key, e);
            Thread.currentThread().interrupt();
            return false;
        } catch (Exception e) {
            log.error("获取锁异常: {}", key, e);
            return false;
        }
    }

    @Override
    public void unlock(String key) {
        try {
            RLock lock = redissonClient.getLock(key);
            if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                log.debug("释放锁: {}", key);
                lock.unlock();
            }
        } catch (Exception e) {
            log.error("释放锁异常: {}", key, e);
        }
    }

    @Override
    public void forceUnlock(String key) {
        try {
            RLock lock = redissonClient.getLock(key);
            if (lock.isLocked()) {
                log.warn("强制释放锁: {}", key);
                lock.forceUnlock();
            }
        } catch (Exception e) {
            log.error("强制释放锁异常: {}", key, e);
        }
    }

    /**
     * 获取锁对象
     *
     * @param key    锁的键
     * @param isFair 是否是公平锁
     * @return 锁对象
     */
    private RLock getLock(String key, boolean isFair) {
        return isFair ? redissonClient.getFairLock(key) : redissonClient.getLock(key);
    }
} 
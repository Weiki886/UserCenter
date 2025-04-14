package com.weiki.usercenterbackend.service;

import java.util.concurrent.TimeUnit;

/**
 * 分布式锁服务接口
 */
public interface DistributedLockService {

    /**
     * 尝试获取锁
     *
     * @param key       锁的键
     * @param waitTime  等待获取锁的时间
     * @param leaseTime 持有锁的时间
     * @param unit      时间单位
     * @param isFair    是否是公平锁
     * @return 是否获取到锁
     */
    boolean tryLock(String key, long waitTime, long leaseTime, TimeUnit unit, boolean isFair);

    /**
     * 释放锁
     *
     * @param key 锁的键
     */
    void unlock(String key);

    /**
     * 强制释放所有锁
     *
     * @param key 锁的键
     */
    void forceUnlock(String key);
} 
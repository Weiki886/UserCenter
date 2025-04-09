package com.weiki.usercenterbackend.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * 分布式锁注解
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DistributedLock {

    /**
     * 锁的名称。如果不指定，则默认为：类名+方法名
     */
    String lockKey() default "";

    /**
     * 锁的前缀
     */
    String lockPrefix() default "weiki:lock:";

    /**
     * 等待锁的时间，默认3秒。
     * 如果获取不到锁，会在该时间内阻塞等待获取锁
     */
    long waitTime() default 3000L;

    /**
     * 锁的持有时间，默认30秒。
     * 超时后自动释放锁
     */
    long leaseTime() default 30000L;

    /**
     * 时间单位，默认为毫秒
     */
    TimeUnit timeUnit() default TimeUnit.MILLISECONDS;

    /**
     * 是否公平锁，默认为非公平锁
     */
    boolean isFair() default false;
} 
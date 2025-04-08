package com.weiki.usercenterbackend.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DistributedLock {
    /**
     * 锁的key
     */
    String key();
    
    /**
     * 锁的等待时间
     */
    long waitTime() default 5;
    
    /**
     * 锁的持有时间
     */
    long leaseTime() default 10;
    
    /**
     * 时间单位
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;
} 
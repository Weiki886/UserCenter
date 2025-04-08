package com.weiki.usercenterbackend.aop;

import com.google.common.util.concurrent.RateLimiter;
import com.weiki.usercenterbackend.annotation.RateLimit;
import com.weiki.usercenterbackend.common.BaseResponse;
import com.weiki.usercenterbackend.common.ErrorCode;
import com.weiki.usercenterbackend.exception.BusinessException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

@Aspect
@Component
public class RateLimitAspect {
    
    private final ConcurrentHashMap<String, RateLimiter> limiters = new ConcurrentHashMap<>();
    
    @Around("@annotation(com.weiki.usercenterbackend.annotation.RateLimit)")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        RateLimit rateLimit = method.getAnnotation(RateLimit.class);
        
        String key = rateLimit.key();
        if (key.isEmpty()) {
            key = method.getDeclaringClass().getName() + "." + method.getName();
        }
        
        RateLimiter limiter = limiters.computeIfAbsent(key, 
            k -> RateLimiter.create(rateLimit.limit() / (double) rateLimit.time()));
            
        if (!limiter.tryAcquire()) {
            throw new BusinessException(ErrorCode.TOO_MANY_REQUESTS, "请求过于频繁，请稍后再试");
        }
        
        return point.proceed();
    }
} 
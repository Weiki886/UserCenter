package com.weiki.usercenterbackend.aop;

import com.weiki.usercenterbackend.annotation.DistributedLock;
import com.weiki.usercenterbackend.common.ErrorCode;
import com.weiki.usercenterbackend.exception.BusinessException;
import com.weiki.usercenterbackend.service.DistributedLockService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.core.annotation.Order;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * 分布式锁切面
 */
@Aspect
@Component
@Slf4j
@Order(0) // 最高优先级，确保锁的获取在其他切面之前
@ConditionalOnBean(DistributedLockService.class)
public class DistributedLockAspect {

    @Resource
    private DistributedLockService distributedLockService;
    
    // SpEL表达式解析器
    private final ExpressionParser parser = new SpelExpressionParser();

    /**
     * 环绕通知，处理带有@DistributedLock注解的方法
     */
    @Around("@annotation(com.weiki.usercenterbackend.annotation.DistributedLock)")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取方法签名
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        
        // 获取类名和方法名
        String className = method.getDeclaringClass().getSimpleName();
        String methodName = method.getName();
        
        // 获取锁注解
        DistributedLock lockAnnotation = method.getAnnotation(DistributedLock.class);
        
        // 获取锁键
        String lockKey = buildLockKey(lockAnnotation, className, methodName, joinPoint);
        
        log.debug("准备获取分布式锁，键: {}", lockKey);
        
        // 尝试获取锁
        boolean locked = false;
        try {
            locked = distributedLockService.tryLock(
                    lockKey,
                    lockAnnotation.waitTime(),
                    lockAnnotation.leaseTime(),
                    lockAnnotation.timeUnit(),
                    lockAnnotation.isFair()
            );
            
            if (!locked) {
                log.warn("获取分布式锁失败，键: {}", lockKey);
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "系统繁忙，请稍后再试");
            }
            
            log.debug("获取分布式锁成功，键: {}", lockKey);
            // 执行实际的方法
            return joinPoint.proceed();
            
        } finally {
            if (locked) {
                distributedLockService.unlock(lockKey);
                log.debug("释放分布式锁，键: {}", lockKey);
            }
        }
    }

    /**
     * 构建锁键，支持SpEL表达式
     */
    private String buildLockKey(DistributedLock lockAnnotation, String className, String methodName, ProceedingJoinPoint joinPoint) {
        String key = lockAnnotation.lockKey();
        
        // 如果是SpEL表达式，则解析表达式
        if (StringUtils.isNotBlank(key) && (key.contains("#") || key.contains("'"))) {
            try {
                key = parseSpEL(key, joinPoint);
            } catch (Exception e) {
                log.error("SpEL表达式解析错误: {}", key, e);
                // 解析失败时使用默认key
                key = className + ":" + methodName;
            }
        } else if (StringUtils.isBlank(key)) {
            // 如果没有指定key，则使用类名+方法名
            key = className + ":" + methodName;
        }
        
        return lockAnnotation.lockPrefix() + key;
    }
    
    /**
     * 解析SpEL表达式
     * 
     * @param spEL SpEL表达式
     * @param joinPoint 连接点
     * @return 解析后的值
     */
    private String parseSpEL(String spEL, ProceedingJoinPoint joinPoint) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        Parameter[] parameters = method.getParameters();
        Object[] args = joinPoint.getArgs();
        
        EvaluationContext context = new StandardEvaluationContext();
        
        // 将方法参数添加到表达式上下文
        for (int i = 0; i < parameters.length; i++) {
            context.setVariable(parameters[i].getName(), args[i]);
        }
        
        Expression expression = parser.parseExpression(spEL);
        return expression.getValue(context, String.class);
    }
} 
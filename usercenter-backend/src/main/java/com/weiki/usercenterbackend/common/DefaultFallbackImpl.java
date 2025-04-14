package com.weiki.usercenterbackend.common;

import com.weiki.usercenterbackend.model.response.BaseResponse;
import com.weiki.usercenterbackend.model.response.ResultCode;
import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 默认限流降级实现
 * 当被限流时返回标准错误响应和部分缓存数据
 */
@Component
public class DefaultFallbackImpl implements Fallback {
    
    private static final Logger logger = LoggerFactory.getLogger(DefaultFallbackImpl.class);
    
    @Override
    public Object fallback(ProceedingJoinPoint joinPoint, RateLimitInfo rateLimitInfo) {
        String className = rateLimitInfo.getClassName();
        String methodName = rateLimitInfo.getMethod();
        long waitTimeMillis = rateLimitInfo.getWaitTimeMillis();
        
        logger.warn("执行降级策略: {}.{} 被限流, 等待时间: {}ms", className, methodName, waitTimeMillis);
        
        // 构造降级响应数据
        Map<String, Object> fallbackData = new HashMap<>();
        fallbackData.put("isFallback", true);
        fallbackData.put("reason", rateLimitInfo.getReason());
        fallbackData.put("timestamp", rateLimitInfo.getTimestamp());
        fallbackData.put("suggestedWaitTime", waitTimeMillis);
        
        // 这里可以添加从缓存获取的数据
        // 注意：实际生产环境应该使用Redis等缓存，这里简化处理
        fallbackData.put("cachedAt", System.currentTimeMillis() - 60000); // 模拟一分钟前的缓存数据
        
        // 返回包含降级数据的响应
        return BaseResponse.error(
                ResultCode.TOO_MANY_REQUESTS,
                "请求频率超限，已返回降级数据，建议" + (waitTimeMillis / 1000) + "秒后重试",
                fallbackData
        );
    }
} 
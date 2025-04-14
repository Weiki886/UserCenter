package com.weiki.usercenterbackend.exception;

import com.weiki.usercenterbackend.model.response.BaseResponse;
import com.weiki.usercenterbackend.model.response.ResultCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * 限流异常处理器
 * 处理API调用触发限流时的异常响应
 */
@RestControllerAdvice
public class RateLimitExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(RateLimitExceptionHandler.class);
    
    /**
     * 处理限流异常
     * 返回标准JSON格式的错误响应并设置Retry-After响应头
     *
     * @param ex 限流异常
     * @param response HTTP响应对象
     * @return 统一格式的错误响应
     */
    @ExceptionHandler(RateLimitException.class)
    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    public BaseResponse<String> handleRateLimitException(RateLimitException ex, HttpServletResponse response) {
        // 记录限流日志
        logger.warn("Rate limit exceeded: {} - {} - retry after {}ms", 
                ex.getLimiterType(), ex.getLimitTarget(), ex.getRetryAfterMs());
        
        // 设置Retry-After响应头，告知客户端需要等待多久
        // 两种格式：秒数或HTTP日期
        long retryAfterSeconds = ex.getRetryAfterSeconds();
        
        // 方式1：使用秒数（更简单）
        response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));
        
        // 方式2：使用HTTP日期格式（更规范）
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        Date retryAfterDate = new Date(System.currentTimeMillis() + (retryAfterSeconds * 1000));
        response.setHeader("X-RateLimit-Reset", dateFormat.format(retryAfterDate));
        
        // 额外的限流信息响应头
        response.setHeader("X-RateLimit-Type", ex.getLimiterType());
        response.setHeader("X-RateLimit-Target", ex.getLimitTarget());
        
        // 构建标准错误响应
        return BaseResponse.error(ResultCode.TOO_MANY_REQUESTS, 
                String.format("请求频率超过限制，请在%d秒后重试", retryAfterSeconds));
    }
} 
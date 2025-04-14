package com.weiki.usercenterbackend.exception;

import com.weiki.usercenterbackend.model.response.BaseResponse;
import com.weiki.usercenterbackend.common.ErrorCode;
import com.weiki.usercenterbackend.common.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     * @param e 业务异常
     * @param request HTTP请求
     * @return 标准化的错误响应
     */
    @ExceptionHandler(BusinessException.class)
    public <T> BaseResponse<T> businessExceptionHandler(BusinessException e, HttpServletRequest request) {
        log.error("业务异常: [{}] {}, 访问路径: {}, 描述: {}", 
                e.getCode(), e.getMessage(), request.getRequestURI(), e.getDescription());
        return ResultUtils.error(e.getCode(), e.getMessage(), e.getDescription());
    }

    /**
     * 处理参数校验异常
     * @param e 参数校验异常
     * @param request HTTP请求
     * @return 标准化的错误响应
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public <T> BaseResponse<T> methodArgumentNotValidExceptionHandler(
            MethodArgumentNotValidException e, HttpServletRequest request) {
        
        // 获取字段错误信息
        BindingResult bindingResult = e.getBindingResult();
        List<FieldError> fieldErrors = bindingResult.getFieldErrors();
        StringBuilder errorMsg = new StringBuilder();
        
        // 拼接所有字段错误
        for (FieldError fieldError : fieldErrors) {
            errorMsg.append(fieldError.getField())
                    .append(": ")
                    .append(fieldError.getDefaultMessage())
                    .append(", ");
        }
        
        // 去除最后的逗号和空格
        String finalErrorMsg = errorMsg.length() > 0 
                ? errorMsg.substring(0, errorMsg.length() - 2) 
                : "参数校验失败";
                
        log.error("参数校验异常: {}, 访问路径: {}", finalErrorMsg, request.getRequestURI());
        
        return ResultUtils.error(ErrorCode.PARAMS_ERROR, "参数不合法", finalErrorMsg);
    }

    /**
     * 处理运行时异常
     * @param e 运行时异常
     * @param request HTTP请求
     * @return 标准化的错误响应
     */
    @ExceptionHandler(RuntimeException.class)
    public <T> BaseResponse<T> runtimeExceptionHandler(RuntimeException e, HttpServletRequest request) {
        log.error("运行时异常: {}, 访问路径: {}", e.getMessage(), request.getRequestURI(), e);
        
        // 对于与用户界面相关的消息，返回友好的错误信息
        String userMessage = "系统错误，请稍后再试";
        String detailMessage = e.getMessage();
        
        // 如果消息中包含SQL等技术细节，不直接展示给用户
        if (detailMessage != null && (
                detailMessage.contains("SQL") || 
                detailMessage.contains("NullPointer") || 
                detailMessage.contains("OutOfMemory"))) {
            detailMessage = "系统内部错误";
        }
        
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, userMessage, detailMessage);
    }
    
    /**
     * 处理所有其他未捕获的异常
     * @param e 异常
     * @param request HTTP请求
     * @return 标准化的错误响应
     */
    @ExceptionHandler(Exception.class)
    public <T> BaseResponse<T> exceptionHandler(Exception e, HttpServletRequest request) {
        log.error("未知异常: {}, 访问路径: {}", e.getMessage(), request.getRequestURI(), e);
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "系统发生未知错误", "请联系管理员");
    }
} 

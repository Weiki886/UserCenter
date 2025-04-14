package com.weiki.usercenterbackend.model.response;

/**
 * API响应状态码枚举
 */
public enum ResultCode {
    
    /**
     * 成功
     */
    SUCCESS(0, "成功"),
    
    /**
     * 请求参数错误
     */
    PARAMS_ERROR(40000, "请求参数错误"),
    
    /**
     * 未登录
     */
    NOT_LOGIN_ERROR(40100, "未登录"),
    
    /**
     * 无权限
     */
    NO_AUTH_ERROR(40101, "无权限"),
    
    /**
     * 请求数据不存在
     */
    NOT_FOUND_ERROR(40400, "请求数据不存在"),
    
    /**
     * 请求频率超限
     */
    TOO_MANY_REQUESTS(42900, "请求频率超限"),
    
    /**
     * 系统内部异常
     */
    SYSTEM_ERROR(50000, "系统内部异常"),
    
    /**
     * 系统繁忙，请稍后重试
     */
    SYSTEM_BUSY(50100, "系统繁忙，请稍后重试");
    
    /**
     * 状态码
     */
    private final int code;
    
    /**
     * 状态码描述
     */
    private final String message;
    
    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
    
    public int getCode() {
        return code;
    }
    
    public String getMessage() {
        return message;
    }
} 
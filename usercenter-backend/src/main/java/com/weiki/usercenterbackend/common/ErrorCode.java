package com.weiki.usercenterbackend.common;

/**
 * 错误码
 */
public enum ErrorCode {

    SUCCESS(0, "成功", ""),
    PARAMS_ERROR(40000, "请求参数错误", ""),
    NULL_ERROR(40001, "请求数据为空", ""),
    NOT_LOGIN(40100, "未登录", ""),
    NO_AUTH(40101, "无权限", ""),
    LOGIN_ERROR(40102, "账号或密码错误", ""),
    OPERATION_ERROR(40103, "操作失败", ""),
    FORBIDDEN(40301, "禁止访问", ""),
    NOT_FOUND(40400, "资源不存在", ""),
    SYSTEM_ERROR(50000, "系统内部异常", "");

    /**
     * 状态码
     */
    private final int code;
    /**
     * 状态码信息
     */
    private final String message;
    /**
     * 状态码描述（详情）
     */
    private final String description;

    ErrorCode(int code, String message, String description) {
        this.code = code;
        this.message = message;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getDescription() {
        return description;
    }
} 
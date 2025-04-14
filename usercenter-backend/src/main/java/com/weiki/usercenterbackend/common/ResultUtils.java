package com.weiki.usercenterbackend.common;

import com.weiki.usercenterbackend.model.response.BaseResponse;

import java.util.Map;

/**
 * 返回工具类
 */
public class ResultUtils {

    /**
     * 成功
     *
     * @param data 数据
     * @param <T> 数据类型
     * @return 响应对象
     */
    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(0, data, "ok", null);
    }

    /**
     * 成功，附带额外信息
     *
     * @param data 数据
     * @param extraInfo 额外信息
     * @param <T> 数据类型
     * @return 响应
     */
    public static <T> BaseResponse<T> success(T data, Map<String, Object> extraInfo) {
        BaseResponse<T> response = new BaseResponse<>(0, data, "ok", null);
        response.setExtraInfo(extraInfo);
        return response;
    }

    /**
     * 成功，自定义消息
     *
     * @param data 数据
     * @param message 消息
     * @param <T> 数据类型
     * @return 响应对象
     */
    public static <T> BaseResponse<T> success(T data, String message) {
        return new BaseResponse<>(0, data, message, null);
    }

    /**
     * 失败
     *
     * @param errorCode 错误码
     * @param <T> 数据类型
     * @return 响应对象
     */
    public static <T> BaseResponse<T> error(ErrorCode errorCode) {
        return new BaseResponse<>(errorCode);
    }

    /**
     * 失败
     *
     * @param code 错误码
     * @param message 错误消息
     * @param description 错误描述
     * @param <T> 数据类型
     * @return 响应对象
     */
    public static <T> BaseResponse<T> error(int code, String message, String description) {
        return new BaseResponse<>(code, null, message, description);
    }

    /**
     * 失败
     *
     * @param errorCode 错误码
     * @param message 自定义错误消息
     * @param description 错误描述
     * @param <T> 数据类型
     * @return 响应对象
     */
    public static <T> BaseResponse<T> error(ErrorCode errorCode, String message, String description) {
        return new BaseResponse<>(errorCode.getCode(), null, message, description);
    }

    /**
     * 失败
     *
     * @param errorCode 错误码
     * @param description 错误描述
     * @param <T> 数据类型
     * @return 响应对象
     */
    public static <T> BaseResponse<T> error(ErrorCode errorCode, String description) {
        return new BaseResponse<>(errorCode.getCode(), null, errorCode.getMessage(), description);
    }
} 
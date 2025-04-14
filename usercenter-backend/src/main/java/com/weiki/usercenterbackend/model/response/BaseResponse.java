package com.weiki.usercenterbackend.model.response;

import com.weiki.usercenterbackend.common.ErrorCode;

import java.io.Serializable;
import java.util.Map;

/**
 * 通用API响应结果
 * @param <T> 响应数据类型
 */
public class BaseResponse<T> implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 响应状态码
     */
    private int code;
    
    /**
     * 响应数据
     */
    private T data;
    
    /**
     * 响应消息
     */
    private String message;
    
    /**
     * 详细描述信息
     */
    private String description;
    
    /**
     * 响应时间戳
     */
    private long timestamp;
    
    /**
     * 请求ID，用于问题排查
     */
    private String requestId;
    
    /**
     * 额外信息，可用于传递非业务数据的元信息
     */
    private Map<String, Object> extraInfo;
    
    /**
     * 默认构造函数
     */
    public BaseResponse() {
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * 带参数的构造函数
     * 
     * @param code 状态码
     * @param data 响应数据
     * @param message 响应消息
     */
    public BaseResponse(int code, T data, String message) {
        this.code = code;
        this.data = data;
        this.message = message;
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * 带错误码的构造函数
     * 
     * @param errorCode 错误码
     */
    public BaseResponse(ErrorCode errorCode) {
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
        this.description = errorCode.getDescription();
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * 完整参数的构造函数
     * 
     * @param code 状态码
     * @param data 响应数据
     * @param message 响应消息
     * @param description 描述信息
     */
    public BaseResponse(int code, T data, String message, String description) {
        this.code = code;
        this.data = data;
        this.message = message;
        this.description = description;
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * 创建成功响应，带数据
     * @param data 响应数据
     * @param <T> 数据类型
     * @return 响应对象
     */
    public static <T> BaseResponse<T> success(T data) {
        BaseResponse<T> response = new BaseResponse<>();
        response.setCode(ResultCode.SUCCESS.getCode());
        response.setData(data);
        response.setMessage(ResultCode.SUCCESS.getMessage());
        return response;
    }
    
    /**
     * 创建成功响应，带数据和额外信息
     * @param data 响应数据
     * @param extraInfo 额外信息
     * @param <T> 数据类型
     * @return 响应对象
     */
    public static <T> BaseResponse<T> success(T data, Map<String, Object> extraInfo) {
        BaseResponse<T> response = success(data);
        response.setExtraInfo(extraInfo);
        return response;
    }
    
    /**
     * 创建成功响应，无数据
     * @param <T> 数据类型
     * @return 响应对象
     */
    public static <T> BaseResponse<T> success() {
        return success(null);
    }
    
    /**
     * 创建错误响应
     * @param resultCode 错误状态码
     * @param message 错误消息
     * @param <T> 数据类型
     * @return 响应对象
     */
    public static <T> BaseResponse<T> error(ResultCode resultCode, String message) {
        BaseResponse<T> response = new BaseResponse<>();
        response.setCode(resultCode.getCode());
        response.setMessage(message);
        return response;
    }
    
    /**
     * 创建错误响应，带数据
     * @param resultCode 错误状态码
     * @param message 错误消息
     * @param data 错误相关数据
     * @param <T> 数据类型
     * @return 响应对象
     */
    public static <T> BaseResponse<T> error(ResultCode resultCode, String message, T data) {
        BaseResponse<T> response = new BaseResponse<>();
        response.setCode(resultCode.getCode());
        response.setMessage(message);
        response.setData(data);
        return response;
    }
    
    /**
     * 创建错误响应，使用状态码默认消息
     * @param resultCode 错误状态码
     * @param <T> 数据类型
     * @return 响应对象
     */
    public static <T> BaseResponse<T> error(ResultCode resultCode) {
        return error(resultCode, resultCode.getMessage());
    }
    
    /**
     * 快速创建系统错误响应
     * @param <T> 数据类型
     * @return 响应对象
     */
    public static <T> BaseResponse<T> systemError() {
        return error(ResultCode.SYSTEM_ERROR);
    }
    
    public int getCode() {
        return code;
    }
    
    public void setCode(int code) {
        this.code = code;
    }
    
    public T getData() {
        return data;
    }
    
    public void setData(T data) {
        this.data = data;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getRequestId() {
        return requestId;
    }
    
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
    
    public Map<String, Object> getExtraInfo() {
        return extraInfo;
    }
    
    public void setExtraInfo(Map<String, Object> extraInfo) {
        this.extraInfo = extraInfo;
    }
} 
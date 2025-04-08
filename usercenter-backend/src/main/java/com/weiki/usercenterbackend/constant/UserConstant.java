package com.weiki.usercenterbackend.constant;

/**
 * 用户常量
 */
public interface UserConstant {

    /**
     * 用户登录态键
     */
    String USER_LOGIN_STATE = "userLoginState";

    /**
     * 普通用户角色
     */
    int DEFAULT_ROLE = 0;

    /**
     * 管理员角色
     */
    int ADMIN_ROLE = 1;

    /**
     * 用户缓存键前缀
     */
    String USER_CACHE_KEY = "user:info:";

    /**
     * 用户缓存过期时间（分钟）
     */
    long USER_CACHE_EXPIRE = 30;
} 
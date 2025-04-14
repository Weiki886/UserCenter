package com.weiki.usercenterbackend.service;

import com.weiki.usercenterbackend.model.dto.TokenDTO;

/**
 * 认证服务接口
 */
public interface AuthService {

    /**
     * 使用用户名和密码登录
     *
     * @param username 用户名
     * @param password 密码
     * @return 令牌DTO
     */
    TokenDTO login(String username, String password);

    /**
     * 使用刷新令牌获取新的访问令牌
     *
     * @param refreshToken 刷新令牌
     * @return 令牌DTO
     */
    TokenDTO refreshToken(String refreshToken);

    /**
     * 验证令牌有效性
     *
     * @param token 令牌
     * @return 是否有效
     */
    boolean validateToken(String token);

    /**
     * 注销用户令牌
     *
     * @param userId 用户ID
     */
    void logout(Long userId);
} 
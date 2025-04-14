package com.weiki.usercenterbackend.service.impl;

import com.weiki.usercenterbackend.constant.JwtConstant;
import com.weiki.usercenterbackend.exception.JwtAuthenticationException;
import com.weiki.usercenterbackend.model.dto.TokenDTO;
import com.weiki.usercenterbackend.service.AuthService;
import com.weiki.usercenterbackend.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * 认证服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final JwtUtils jwtUtils;
    private final RedisTemplate<String, Object> redisTemplate;

    // Redis中存储黑名单令牌的前缀
    private static final String TOKEN_BLACKLIST_PREFIX = "token:blacklist:";
    // Redis中存储刷新令牌的前缀
    private static final String REFRESH_TOKEN_PREFIX = "token:refresh:";

    @Override
    public TokenDTO login(String username, String password) {
        // 注意：这里应调用用户服务验证用户名和密码
        // 为了简化示例，假设验证成功，并返回用户ID
        Long userId = getUserIdByUsernameAndPassword(username, password);

        // 生成访问令牌和刷新令牌
        String accessToken = jwtUtils.generateAccessToken(userId);
        String refreshToken = jwtUtils.generateRefreshToken(userId);

        // 将刷新令牌存储到Redis，有效期与令牌相同
        storeRefreshToken(userId, refreshToken);

        // 构建并返回令牌DTO
        return buildTokenDto(accessToken, refreshToken);
    }

    @Override
    public TokenDTO refreshToken(String refreshToken) {
        // 验证刷新令牌
        if (!jwtUtils.validateToken(refreshToken) || !jwtUtils.isRefreshToken(refreshToken)) {
            throw new JwtAuthenticationException("无效的刷新令牌");
        }

        // 从刷新令牌中获取用户ID
        Optional<String> userIdStrOpt = jwtUtils.getUserIdFromToken(refreshToken);
        if (userIdStrOpt.isEmpty()) {
            throw new JwtAuthenticationException("无法从刷新令牌中获取用户ID");
        }
        
        Long userId;
        try {
            // 将String类型的userId转换为Long类型
            userId = Long.parseLong(userIdStrOpt.get());
        } catch (NumberFormatException e) {
            log.error("用户ID格式错误，无法转换为Long类型", e);
            throw new JwtAuthenticationException("令牌中的用户ID格式无效");
        }
        
        // 验证Redis中是否存在此刷新令牌
        String storedRefreshToken = getStoredRefreshToken(userId);
        if (storedRefreshToken == null || !storedRefreshToken.equals(refreshToken)) {
            throw new JwtAuthenticationException("刷新令牌已失效，请重新登录");
        }

        // 生成新的访问令牌和刷新令牌
        String newAccessToken = jwtUtils.generateAccessToken(userId);
        String newRefreshToken = jwtUtils.generateRefreshToken(userId);

        // 更新Redis中的刷新令牌
        storeRefreshToken(userId, newRefreshToken);

        // 构建并返回令牌DTO
        return buildTokenDto(newAccessToken, newRefreshToken);
    }

    @Override
    public boolean validateToken(String token) {
        // 首先检查令牌是否在黑名单中
        if (isTokenBlacklisted(token)) {
            return false;
        }
        // 然后验证令牌的签名和有效期
        return jwtUtils.validateToken(token);
    }

    @Override
    public void logout(Long userId) {
        // 从Redis中删除刷新令牌
        removeRefreshToken(userId);
        
        // 可以在这里添加将当前用户的访问令牌加入黑名单的逻辑
        // 但需要获取当前的访问令牌，该方法参数中没有提供
    }

    /**
     * 将令牌添加到黑名单
     *
     * @param token 令牌
     * @param expirationTime 过期时间（毫秒）
     */
    private void addTokenToBlacklist(String token, long expirationTime) {
        String blacklistKey = TOKEN_BLACKLIST_PREFIX + token;
        redisTemplate.opsForValue().set(blacklistKey, "blacklisted", expirationTime, TimeUnit.MILLISECONDS);
    }

    /**
     * 检查令牌是否在黑名单中
     *
     * @param token 令牌
     * @return 是否在黑名单中
     */
    private boolean isTokenBlacklisted(String token) {
        String blacklistKey = TOKEN_BLACKLIST_PREFIX + token;
        return Boolean.TRUE.equals(redisTemplate.hasKey(blacklistKey));
    }

    /**
     * 存储刷新令牌到Redis
     *
     * @param userId 用户ID
     * @param refreshToken 刷新令牌
     */
    private void storeRefreshToken(Long userId, String refreshToken) {
        String refreshTokenKey = REFRESH_TOKEN_PREFIX + userId;
        redisTemplate.opsForValue().set(refreshTokenKey, refreshToken, JwtConstant.REFRESH_TOKEN_EXPIRATION, TimeUnit.MILLISECONDS);
    }

    /**
     * 从Redis获取存储的刷新令牌
     *
     * @param userId 用户ID
     * @return 刷新令牌
     */
    private String getStoredRefreshToken(Long userId) {
        String refreshTokenKey = REFRESH_TOKEN_PREFIX + userId;
        Object token = redisTemplate.opsForValue().get(refreshTokenKey);
        return token != null ? token.toString() : null;
    }

    /**
     * 从Redis删除刷新令牌
     *
     * @param userId 用户ID
     */
    private void removeRefreshToken(Long userId) {
        String refreshTokenKey = REFRESH_TOKEN_PREFIX + userId;
        redisTemplate.delete(refreshTokenKey);
    }

    /**
     * 根据用户名和密码获取用户ID
     * 注意：这是一个模拟方法，实际应用中应调用用户服务
     *
     * @param username 用户名
     * @param password 密码
     * @return 用户ID
     */
    private Long getUserIdByUsernameAndPassword(String username, String password) {
        // 这里应调用用户服务验证用户名和密码
        // 为了简化示例，假设验证成功，并返回用户ID 1L
        return 1L; // 模拟返回用户ID
    }

    /**
     * 构建令牌DTO
     *
     * @param accessToken 访问令牌
     * @param refreshToken 刷新令牌
     * @return 令牌DTO
     */
    private TokenDTO buildTokenDto(String accessToken, String refreshToken) {
        return TokenDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(JwtConstant.ACCESS_TOKEN_EXPIRATION / 1000) // 转换为秒
                .build();
    }
} 
package com.weiki.usercenterbackend.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 令牌DTO，用于向客户端返回访问令牌和刷新令牌
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenDTO {
    /**
     * 访问令牌
     */
    private String accessToken;
    
    /**
     * 刷新令牌
     */
    private String refreshToken;
    
    /**
     * 令牌类型
     */
    private String tokenType;
    
    /**
     * 过期时间（秒）
     */
    private long expiresIn;
} 
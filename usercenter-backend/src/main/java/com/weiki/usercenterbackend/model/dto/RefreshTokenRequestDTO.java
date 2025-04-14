package com.weiki.usercenterbackend.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 刷新令牌请求DTO
 */
@Data
public class RefreshTokenRequestDTO {

    /**
     * 刷新令牌
     */
    @NotBlank(message = "刷新令牌不能为空")
    private String refreshToken;
} 
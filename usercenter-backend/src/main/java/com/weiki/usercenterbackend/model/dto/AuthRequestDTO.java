package com.weiki.usercenterbackend.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 认证请求DTO
 */
@Data
public class AuthRequestDTO {

    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    private String username;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    private String password;
}
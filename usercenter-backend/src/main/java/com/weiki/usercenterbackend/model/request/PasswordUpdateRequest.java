package com.weiki.usercenterbackend.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 密码更新请求体
 */
@Data
public class PasswordUpdateRequest implements Serializable {

    private static final long serialVersionUID = 3191241716373120794L;

    /**
     * 旧密码
     */
    private String oldPassword;

    /**
     * 新密码
     */
    private String newPassword;

    /**
     * 确认密码
     */
    private String checkPassword;
} 
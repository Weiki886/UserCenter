package com.weiki.usercenterbackend.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户更新请求体
 */
@Data
public class UserUpdateRequest implements Serializable {

    private static final long serialVersionUID = 3191241716373120793L;

    /**
     * 用户id（仅管理员可指定其他用户id）
     */
    private Long id;

    /**
     * 用户昵称
     */
    private String username;

    /**
     * 用户头像
     */
    private String avatarUrl;

    /**
     * 性别
     */
    private Integer gender;

    /**
     * 电话
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;
} 
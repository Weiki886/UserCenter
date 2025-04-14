package com.weiki.usercenterbackend.model.domain;

import lombok.Data;

import java.util.Date;

/**
 * 用户实体类
 */
@Data
public class User {
    /**
     * 用户ID，主键
     */
    private Long id;

    /**
     * 用户昵称
     */
    private String username;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 用户头像
     */
    private String avatarUrl;

    /**
     * 性别
     */
    private Integer gender;

    /**
     * 密码
     */
    private String userPassword;

    /**
     * 电话
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 用户状态 0 - 正常 1 - 封禁
     */
    private Integer userStatus;

    /**
     * 解封时间
     */
    private Date unbannedTime;
    
    /**
     * 是否被封禁 0 - 否 1 - 是
     */
    private Integer isBanned;
    
    /**
     * 解封日期，null表示永久封禁
     */
    private Date unbanDate;
    
    /**
     * 封禁原因
     */
    private String banReason;

    /**
     * 创建时间
     */
    private Date createTime;
    
    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除(0-未删, 1-已删)
     */
    private Integer isDelete;

    /**
     * 用户角色 0 - 普通用户 1 - 管理员
     */
    private Integer userRole;
} 
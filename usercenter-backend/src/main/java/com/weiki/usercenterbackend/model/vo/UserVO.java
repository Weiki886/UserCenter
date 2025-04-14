package com.weiki.usercenterbackend.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户视图对象（脱敏）
 */
@Data
public class UserVO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 用户ID
     */
    private Long id;
    
    /**
     * 用户账号
     */
    private String userAccount;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 用户头像URL
     */
    private String avatarUrl;
    
    /**
     * 性别（0-男，1-女）
     */
    private Integer gender;
    
    /**
     * 联系电话
     */
    private String phone;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 用户角色（0-普通用户，1-管理员）
     */
    private Integer userRole;
    
    /**
     * 用户状态（0-正常）
     */
    private Integer userStatus;
    
    /**
     * 是否封禁（0-未封禁，1-已封禁）
     */
    private Integer isBanned;
    
    /**
     * 解封日期（如果已封禁）
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
} 
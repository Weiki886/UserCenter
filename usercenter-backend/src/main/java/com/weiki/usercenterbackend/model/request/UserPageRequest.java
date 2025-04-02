package com.weiki.usercenterbackend.model.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 用户分页请求
 */
@Data
@ApiModel(description = "用户分页请求")
public class UserPageRequest {

    /**
     * 当前页码
     */
    @ApiModelProperty(value = "当前页码", example = "1")
    private long current = 1;

    /**
     * 页面大小
     */
    @ApiModelProperty(value = "页面大小", example = "10")
    private long pageSize = 10;

    /**
     * 用户名（模糊查询）
     */
    @ApiModelProperty(value = "用户名（模糊查询）")
    private String username;

    /**
     * 账号（模糊查询）
     */
    @ApiModelProperty(value = "账号（模糊查询）")
    private String userAccount;

    /**
     * 用户角色（0表示普通用户，1表示管理员）
     */
    @ApiModelProperty(value = "用户角色")
    private Integer userRole;
} 
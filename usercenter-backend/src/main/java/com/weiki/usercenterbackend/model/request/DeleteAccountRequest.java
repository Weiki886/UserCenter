package com.weiki.usercenterbackend.model.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 用户注销请求
 */
@Data
@ApiModel(description = "用户注销请求")
public class DeleteAccountRequest {

    /**
     * 用户账号
     */
    @ApiModelProperty(value = "用户账号", required = true)
    private String userAccount;

    /**
     * 用户密码
     */
    @ApiModelProperty(value = "用户密码", required = true)
    private String userPassword;
} 
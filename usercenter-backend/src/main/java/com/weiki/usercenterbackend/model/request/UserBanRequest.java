package com.weiki.usercenterbackend.model.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 用户封禁请求
 */
@Data
@ApiModel(description = "用户封禁请求")
public class UserBanRequest {
    
    /**
     * 用户ID
     */
    @ApiModelProperty(value = "用户ID", required = true)
    private Long userId;
    
    /**
     * 封禁天数，0表示永久封禁
     */
    @ApiModelProperty(value = "封禁天数，0表示永久封禁", required = true)
    private Integer banDays;
    
    /**
     * 封禁原因
     */
    @ApiModelProperty(value = "封禁原因", required = true)
    private String reason;
} 
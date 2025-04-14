package com.weiki.usercenterbackend.mq.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户相关消息模型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserMessage implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 消息ID
     */
    private String messageId;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 消息类型：register-注册，login-登录，etc
     */
    private String type;
    
    /**
     * 消息内容
     */
    private String content;
    
    /**
     * 附加数据，可以存放JSON等
     */
    private String extraData;
    
    /**
     * 消息创建时间
     */
    private Date createTime;
} 
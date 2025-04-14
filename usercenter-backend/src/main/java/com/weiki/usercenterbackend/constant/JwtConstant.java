package com.weiki.usercenterbackend.constant;

/**
 * JWT 相关常量
 */
public class JwtConstant {
    /**
     * 密钥
     */
    public static final String JWT_SECRET_KEY = "WeiKiUserCenterSecretKey12345678901234567890";
    
    /**
     * token前缀
     */
    public static final String TOKEN_PREFIX = "Bearer ";
    
    /**
     * 请求头中token的key
     */
    public static final String TOKEN_HEADER = "Authorization";
    
    /**
     * 发行者
     */
    public static final String ISSUER = "weiki-user-center";
    
    /**
     * 访问令牌过期时间（毫秒） - 30分钟
     */
    public static final long ACCESS_TOKEN_EXPIRATION = 30 * 60 * 1000L;
    
    /**
     * 刷新令牌过期时间（毫秒） - 7天
     */
    public static final long REFRESH_TOKEN_EXPIRATION = 7 * 24 * 60 * 60 * 1000L;
    
    /**
     * 用户ID的claim名称
     */
    public static final String USER_ID_CLAIM = "userId";
    
    /**
     * 令牌类型的claim名称
     */
    public static final String TOKEN_TYPE_CLAIM = "tokenType";
    
    /**
     * 访问令牌类型
     */
    public static final String ACCESS_TOKEN_TYPE = "access";
    
    /**
     * 刷新令牌类型
     */
    public static final String REFRESH_TOKEN_TYPE = "refresh";
} 
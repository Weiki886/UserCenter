package com.weiki.usercenterbackend.utils;

import com.weiki.usercenterbackend.constant.JwtConstant;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * JWT工具类，用于生成和验证JWT令牌
 */
@Slf4j
@Component
public class JwtUtils {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    /**
     * 用户ID在JWT中的声明名称
     */
    private static final String USER_ID_CLAIM = "userId";

    /**
     * JWT密钥，从配置中读取
     */
    @Value("${jwt.secret:defaultSecretKeyForDevelopmentEnvironmentOnly}")
    private String jwtSecret;

    /**
     * 静态密钥，用于类的静态方法
     */
    private static SecretKey SECRET_KEY;

    /**
     * 初始化密钥
     */
    @PostConstruct
    public void init() {
        SECRET_KEY = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        logger.info("JWT工具类初始化完成");
    }

    /**
     * 生成访问令牌
     *
     * @param userId 用户ID
     * @return 访问令牌
     */
    public String generateAccessToken(Long userId) {
        return generateToken(userId, JwtConstant.ACCESS_TOKEN_TYPE, JwtConstant.ACCESS_TOKEN_EXPIRATION);
    }

    /**
     * 生成刷新令牌
     *
     * @param userId 用户ID
     * @return 刷新令牌
     */
    public String generateRefreshToken(Long userId) {
        return generateToken(userId, JwtConstant.REFRESH_TOKEN_TYPE, JwtConstant.REFRESH_TOKEN_EXPIRATION);
    }

    /**
     * 生成JWT令牌
     *
     * @param userId     用户ID
     * @param tokenType  令牌类型
     * @param expiration 过期时间
     * @return JWT令牌
     */
    private String generateToken(Long userId, String tokenType, long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtConstant.USER_ID_CLAIM, userId);
        claims.put(JwtConstant.TOKEN_TYPE_CLAIM, tokenType);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(String.valueOf(userId))
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .setIssuer(JwtConstant.ISSUER)
                .signWith(SECRET_KEY)
                .compact();
    }

    /**
     * 从JWT Token中解析获取用户ID
     *
     * @param token JWT Token
     * @return 用户ID，如果解析失败返回空
     */
    public Optional<String> getUserIdFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            
            Object userId = claims.get(USER_ID_CLAIM);
            if (userId != null) {
                return Optional.of(userId.toString());
            }
            return Optional.empty();
        } catch (Exception e) {
            logger.warn("解析JWT Token失败: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * 判断令牌是否为访问令牌
     *
     * @param token JWT令牌
     * @return 是否为访问令牌
     */
    public boolean isAccessToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            String tokenType = claims.get(JwtConstant.TOKEN_TYPE_CLAIM, String.class);
            return JwtConstant.ACCESS_TOKEN_TYPE.equals(tokenType);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 判断令牌是否为刷新令牌
     *
     * @param token JWT令牌
     * @return 是否为刷新令牌
     */
    public boolean isRefreshToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            String tokenType = claims.get(JwtConstant.TOKEN_TYPE_CLAIM, String.class);
            return JwtConstant.REFRESH_TOKEN_TYPE.equals(tokenType);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 验证令牌
     *
     * @param token JWT令牌
     * @return 验证结果
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (SignatureException e) {
            log.error("无效的JWT签名: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("无效的JWT令牌: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT令牌已过期: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("不支持的JWT令牌: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims字符串为空: {}", e.getMessage());
        }
        return false;
    }

    /**
     * 判断令牌是否过期
     *
     * @param token JWT令牌
     * @return 是否过期
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims.getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        } catch (Exception e) {
            log.error("判断令牌是否过期时发生错误", e);
            return true;
        }
    }

    /**
     * 从令牌中获取Claims
     *
     * @param token JWT令牌
     * @return Claims
     */
    private Claims getClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
} 
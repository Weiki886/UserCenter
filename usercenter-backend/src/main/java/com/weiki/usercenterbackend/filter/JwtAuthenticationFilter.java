package com.weiki.usercenterbackend.filter;

import com.weiki.usercenterbackend.constant.JwtConstant;
import com.weiki.usercenterbackend.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * JWT认证过滤器，用于验证请求头中的JWT令牌
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /**
     * 不需要认证的请求路径
     */
    private static final List<String> EXCLUDED_PATHS = Arrays.asList(
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/refresh",
            "/swagger-ui.html",
            "/swagger-ui/",
            "/v3/api-docs",
            "/doc.html"
    );

    private final JwtUtils jwtUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            // 如果是不需要认证的路径，直接放行
            String path = request.getServletPath();
            if (isExcludedPath(path)) {
                filterChain.doFilter(request, response);
                return;
            }

            // 从请求头获取JWT令牌
            String token = getTokenFromRequest(request);
            if (token != null && jwtUtils.validateToken(token)) {
                // 确保令牌是访问令牌而不是刷新令牌
                if (!jwtUtils.isAccessToken(token)) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("无效的令牌类型");
                    return;
                }

                // 从令牌中获取用户ID
                Optional<String> userIdStrOpt = jwtUtils.getUserIdFromToken(token);
                if (userIdStrOpt.isPresent()) {
                    try {
                        // 将String类型的userId转换为Long类型
                        Long userId = Long.parseLong(userIdStrOpt.get());
                        // 将用户ID设置为请求属性，便于后续处理
                        request.setAttribute("userId", userId);
                        filterChain.doFilter(request, response);
                        return;
                    } catch (NumberFormatException e) {
                        log.error("用户ID格式错误，无法转换为Long类型", e);
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.getWriter().write("用户ID格式错误");
                        return;
                    }
                }
            }

            // 认证失败
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("认证失败");
        } catch (Exception e) {
            log.error("认证过程中发生错误", e);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("认证过程中发生错误");
        }
    }

    /**
     * 判断请求路径是否在排除列表中
     *
     * @param path 请求路径
     * @return 是否在排除列表中
     */
    private boolean isExcludedPath(String path) {
        return EXCLUDED_PATHS.stream().anyMatch(path::startsWith);
    }

    /**
     * 从请求头获取JWT令牌
     *
     * @param request HTTP请求
     * @return JWT令牌，如果没有则返回null
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(JwtConstant.TOKEN_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(JwtConstant.TOKEN_PREFIX)) {
            return bearerToken.substring(JwtConstant.TOKEN_PREFIX.length());
        }
        return null;
    }
} 
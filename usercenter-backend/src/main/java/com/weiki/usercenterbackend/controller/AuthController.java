package com.weiki.usercenterbackend.controller;

import com.weiki.usercenterbackend.model.dto.AuthRequestDTO;
import com.weiki.usercenterbackend.model.dto.RefreshTokenRequestDTO;
import com.weiki.usercenterbackend.model.dto.TokenDTO;
import com.weiki.usercenterbackend.service.AuthService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * 认证控制器
 */
@Api(tags = "认证接口")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 用户登录
     *
     * @param authRequest 认证请求DTO
     * @return 令牌DTO
     */
    @PostMapping("/login")
    @ApiOperation("用户登录")
    public ResponseEntity<TokenDTO> login(@Valid @RequestBody AuthRequestDTO authRequest) {
        TokenDTO tokenDTO = authService.login(authRequest.getUsername(), authRequest.getPassword());
        return ResponseEntity.ok(tokenDTO);
    }

    /**
     * 刷新令牌
     *
     * @param refreshTokenRequest 刷新令牌请求DTO
     * @return 新的令牌DTO
     */
    @PostMapping("/refresh")
    @ApiOperation("刷新令牌")
    public ResponseEntity<TokenDTO> refreshToken(@Valid @RequestBody RefreshTokenRequestDTO refreshTokenRequest) {
        TokenDTO tokenDTO = authService.refreshToken(refreshTokenRequest.getRefreshToken());
        return ResponseEntity.ok(tokenDTO);
    }

    /**
     * 用户注销
     *
     * @param userId 用户ID，从请求属性中获取（由JWT过滤器设置）
     * @return 成功响应
     */
    @PostMapping("/logout")
    @ApiOperation("用户注销")
    public ResponseEntity<Void> logout(@RequestAttribute("userId") Long userId) {
        authService.logout(userId);
        return ResponseEntity.ok().build();
    }
} 
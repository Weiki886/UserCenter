package com.weiki.usercenterbackend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * JWT认证异常
 */
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class JwtAuthenticationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public JwtAuthenticationException(String message) {
        super(message);
    }

    public JwtAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
} 
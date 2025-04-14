package com.weiki.usercenterbackend.aop;

import com.weiki.usercenterbackend.annotation.AuthCheck;
import com.weiki.usercenterbackend.common.ErrorCode;
import com.weiki.usercenterbackend.exception.BusinessException;
import com.weiki.usercenterbackend.model.domain.User;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

import static com.weiki.usercenterbackend.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 权限校验 AOP
 */
@Aspect
@Component
@Slf4j
public class AuthInterceptor {

    /**
     * 执行拦截
     *
     * @param joinPoint
     * @param authCheck
     * @return
     * @throws Throwable
     */
    @Around("@annotation(authCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        // 获取当前角色值
        int mustRole = authCheck.mustRole();
        // 获取当前请求的 request
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        // 当前登录用户
        User loginUser = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
        
        // 如果未登录，抛出异常
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        
        // 如果用户被禁用，不允许操作
        if (loginUser.getUserStatus() != 0) {
            throw new BusinessException(ErrorCode.NO_AUTH, "用户状态异常");
        }
        
        // 必须有对应权限才能通过
        Integer userRole = loginUser.getUserRole();
        // 如果需要管理员权限而当前用户不是管理员，则抛出异常
        if (mustRole == 1 && userRole != 1) {
            throw new BusinessException(ErrorCode.NO_AUTH, "无管理员权限");
        }
        
        // 通过权限校验，放行
        return joinPoint.proceed();
    }
} 
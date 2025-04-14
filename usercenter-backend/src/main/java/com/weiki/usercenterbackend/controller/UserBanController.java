package com.weiki.usercenterbackend.controller;

import com.weiki.usercenterbackend.annotation.AuthCheck;
import com.weiki.usercenterbackend.common.ErrorCode;
import com.weiki.usercenterbackend.common.ResultUtils;
import com.weiki.usercenterbackend.exception.BusinessException;
import com.weiki.usercenterbackend.model.domain.User;
import com.weiki.usercenterbackend.model.request.UserBanRequest;
import com.weiki.usercenterbackend.model.response.BaseResponse;
import com.weiki.usercenterbackend.model.vo.PageVO;
import com.weiki.usercenterbackend.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 用户封禁接口
 */
@RestController
@RequestMapping("/user/ban")
@Slf4j
@Api(tags = "用户封禁相关接口")
public class UserBanController {

    @Resource
    private UserService userService;

    /**
     * 封禁用户
     *
     * @param banRequest 封禁请求
     * @param request HTTP请求
     * @return 是否成功
     */
    @PostMapping("/ban")
    @ApiOperation(value = "封禁用户", notes = "管理员封禁用户")
    @AuthCheck(mustRole = 1)
    public BaseResponse<Boolean> banUser(@RequestBody UserBanRequest banRequest, HttpServletRequest request) {
        if (banRequest == null || banRequest.getUserId() == null || banRequest.getBanDays() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        
        long userId = banRequest.getUserId();
        int banDays = banRequest.getBanDays();
        String reason = banRequest.getReason();
        
        if (userId <= 0 || banDays < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }
        
        if (StringUtils.isBlank(reason)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "封禁原因不能为空");
        }
        
        // 先检查要封禁的用户是否为管理员
        User targetUser = userService.getById(userId);
        if (targetUser != null && targetUser.getUserRole() != null && targetUser.getUserRole() == 1) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "管理员无法被封禁");
        }
        
        boolean result = userService.banUser(userId, banDays, reason, request);
        return ResultUtils.success(result);
    }
    
    /**
     * 解封用户
     *
     * @param unbanRequest 包含用户id的请求对象
     * @param request HTTP请求
     * @return 是否成功
     */
    @PostMapping("/unban")
    @ApiOperation(value = "解封用户", notes = "管理员解封用户")
    @AuthCheck(mustRole = 1)
    public BaseResponse<Boolean> unbanUser(@RequestBody Map<String, Long> unbanRequest, HttpServletRequest request) {
        Long userId = unbanRequest.get("userId");
        if (userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        
        boolean result = userService.unbanUser(userId, request);
        return ResultUtils.success(result);
    }
    
    /**
     * 获取封禁用户列表
     *
     * @param current 当前页
     * @param pageSize 页面大小
     * @param request HTTP请求
     * @return 封禁用户列表
     */
    @GetMapping("/list")
    @ApiOperation(value = "获取封禁用户列表", notes = "管理员获取封禁用户列表")
    @AuthCheck(mustRole = 1)
    public BaseResponse<PageVO<User>> getBannedUsers(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long pageSize,
            HttpServletRequest request) {
        
        PageVO<User> bannedUsers = userService.getBannedUserPage(current, pageSize);
        return ResultUtils.success(bannedUsers);
    }
} 
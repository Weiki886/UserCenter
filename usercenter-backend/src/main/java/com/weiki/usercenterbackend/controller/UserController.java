package com.weiki.usercenterbackend.controller;

import com.weiki.usercenterbackend.annotation.AuthCheck;
import com.weiki.usercenterbackend.common.BaseResponse;
import com.weiki.usercenterbackend.common.ErrorCode;
import com.weiki.usercenterbackend.common.ResultUtils;
import com.weiki.usercenterbackend.exception.BusinessException;
import com.weiki.usercenterbackend.model.domain.User;
import com.weiki.usercenterbackend.model.request.UserLoginRequest;
import com.weiki.usercenterbackend.model.request.UserPageRequest;
import com.weiki.usercenterbackend.model.request.UserRegisterRequest;
import com.weiki.usercenterbackend.model.request.UserUpdateRequest;
import com.weiki.usercenterbackend.model.request.PasswordUpdateRequest;
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

import static com.weiki.usercenterbackend.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户接口
 */
@RestController
@RequestMapping("/user")
@Slf4j
@Api(tags = "用户相关接口")
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 用户注册
     *
     * @param userRegisterRequest
     * @param request
     * @return
     */
    @PostMapping("/register")
    @ApiOperation(value = "用户注册", notes = "用户注册接口")
    public BaseResponse<User> userRegister(@RequestBody UserRegisterRequest userRegisterRequest, HttpServletRequest request) {
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.userRegister(userAccount, userPassword, checkPassword, request);
        return ResultUtils.success(user);
    }

    /**
     * 用户登录
     *
     * @param userLoginRequest
     * @param request
     * @return
     */
    @PostMapping("/login")
    @ApiOperation(value = "用户登录", notes = "用户登录接口")
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(user);
    }

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    @PostMapping("/logout")
    @ApiOperation(value = "用户注销", notes = "用户注销接口")
    public BaseResponse<Integer> userLogout(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        int result = userService.userLogout(request);
        return ResultUtils.success(result);
    }

    /**
     * 获取当前用户
     *
     * @param request
     * @return
     */
    @GetMapping("/current")
    @ApiOperation(value = "获取当前用户", notes = "获取当前登录用户的信息")
    public BaseResponse<User> getCurrentUser(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        long userId = currentUser.getId();
        // TODO 校验用户是否合法
        User user = userService.getById(userId);
        User safetyUser = userService.getSafetyUser(user);
        return ResultUtils.success(safetyUser);
    }

    /**
     * 管理员删除用户
     *
     * @param deleteRequest 包含用户id的请求对象
     * @param request HTTP请求
     * @return 是否删除成功
     */
    @PostMapping("/delete")
    @ApiOperation(value = "删除用户", notes = "管理员删除用户")
    @AuthCheck(mustRole = 1)
    public BaseResponse<Boolean> deleteUser(@RequestBody Map<String, Long> deleteRequest, HttpServletRequest request) {
        Long id = deleteRequest.get("id");
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        
        boolean result = userService.deleteUser(id);
        return ResultUtils.success(result);
    }

    /**
     * 更新用户信息
     * 
     * @param updateRequest
     * @param request
     * @return
     */
    @PostMapping("/update")
    @ApiOperation(value = "更新用户信息", notes = "更新当前登录用户的信息")
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest updateRequest, HttpServletRequest request) {
        if (updateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = userService.updateUser(updateRequest, request);
        return ResultUtils.success(result);
    }
    
    /**
     * 分页获取用户列表（仅管理员可操作）
     *
     * @param userPageRequest
     * @param request
     * @return
     */
    @GetMapping("/list/page")
    @ApiOperation(value = "分页获取用户列表", notes = "管理员分页获取用户列表")
    @AuthCheck(mustRole = 1)
    public BaseResponse<PageVO<User>> listUsers(UserPageRequest userPageRequest, HttpServletRequest request) {
        if (userPageRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        
        long current = userPageRequest.getCurrent();
        long pageSize = userPageRequest.getPageSize();
        String username = userPageRequest.getUsername();
        String userAccount = userPageRequest.getUserAccount();
        Integer userRole = userPageRequest.getUserRole();
        
        // 获取分页用户数据
        PageVO<User> userPage = userService.getUserPage(current, pageSize, username, userAccount, userRole);
        return ResultUtils.success(userPage);
    }

    /**
     * 修改密码
     *
     * @param passwordUpdateRequest
     * @param request
     * @return
     */
    @PostMapping("/update-password")
    @ApiOperation(value = "修改密码", notes = "用户修改自己的密码")
    public BaseResponse<Boolean> updatePassword(@RequestBody PasswordUpdateRequest passwordUpdateRequest, HttpServletRequest request) {
        if (passwordUpdateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        
        String oldPassword = passwordUpdateRequest.getOldPassword();
        String newPassword = passwordUpdateRequest.getNewPassword();
        String checkPassword = passwordUpdateRequest.getCheckPassword();
        
        boolean result = userService.updatePassword(oldPassword, newPassword, checkPassword, request);
        return ResultUtils.success(result);
    }
} 
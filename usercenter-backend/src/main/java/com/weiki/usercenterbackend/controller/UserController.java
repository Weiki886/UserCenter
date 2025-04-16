package com.weiki.usercenterbackend.controller;

import com.weiki.usercenterbackend.annotation.AuthCheck;
import com.weiki.usercenterbackend.common.ErrorCode;
import com.weiki.usercenterbackend.common.ResultUtils;
import com.weiki.usercenterbackend.exception.BusinessException;
import com.weiki.usercenterbackend.model.domain.User;
import com.weiki.usercenterbackend.model.request.UserLoginRequest;
import com.weiki.usercenterbackend.model.request.UserPageRequest;
import com.weiki.usercenterbackend.model.request.UserRegisterRequest;
import com.weiki.usercenterbackend.model.request.UserUpdateRequest;
import com.weiki.usercenterbackend.model.request.PasswordUpdateRequest;
import com.weiki.usercenterbackend.model.request.DeleteAccountRequest;
import com.weiki.usercenterbackend.model.response.BaseResponse;
import com.weiki.usercenterbackend.model.vo.PageVO;
import com.weiki.usercenterbackend.model.vo.UserVO;
import com.weiki.usercenterbackend.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    /**
     * 用户注销自己的账号
     *
     * @param deleteAccountRequest 包含用户账号和密码的请求
     * @return 是否删除成功
     */
    @PostMapping("/delete-account")
    @ApiOperation(value = "注销账号", notes = "用户注销自己的账号")
    public BaseResponse<Boolean> deleteAccount(@RequestBody DeleteAccountRequest deleteAccountRequest, HttpServletRequest request) {
        if (deleteAccountRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        
        String userAccount = deleteAccountRequest.getUserAccount();
        String userPassword = deleteAccountRequest.getUserPassword();
        
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号和密码不能为空");
        }
        
        boolean result = userService.deleteAccount(userAccount, userPassword, request);
        return ResultUtils.success(result);
    }

    /**
     * 根据ID获取用户信息
     *
     * @param id 用户id
     * @return 用户信息
     */
    @GetMapping("/{id}")
    public BaseResponse<UserVO> getUserById(@PathVariable Long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return ResultUtils.success(userVO);
    }
    
    /**
     * 根据ID获取用户信息（优先从缓存获取）
     *
     * @param id 用户id
     * @return 用户信息
     */
    @GetMapping("/cache/{id}")
    public BaseResponse<UserVO> getUserByIdWithCache(@PathVariable Long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        
        long startTime = System.currentTimeMillis();
        User user = userService.getByIdWithCache(id);
        long endTime = System.currentTimeMillis();
        
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        
        // 添加查询耗时信息
        Map<String, Object> extraInfo = new HashMap<>();
        extraInfo.put("queryTime", (endTime - startTime) + "ms");
        
        return ResultUtils.success(userVO, extraInfo);
    }
    
    /**
     * 批量获取用户信息
     *
     * @param userIds 用户ID集合
     * @return 用户信息列表
     */
    @GetMapping("/batch")
    public BaseResponse<List<UserVO>> getBatchUsers(@RequestParam("ids") List<Long> userIds) {
        if (CollectionUtils.isEmpty(userIds)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        
        List<User> userList = userService.getUsersByIds(userIds);
        List<UserVO> userVOList = userList.stream().map(user -> {
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(user, userVO);
            return userVO;
        }).collect(Collectors.toList());
        
        return ResultUtils.success(userVOList);
    }
    
    /**
     * 批量获取用户信息（优先从缓存获取）
     *
     * @param userIds 用户ID集合
     * @return 用户信息列表
     */
    @GetMapping("/cache/batch")
    public BaseResponse<List<UserVO>> getBatchUsersWithCache(@RequestParam("ids") List<Long> userIds) {
        if (CollectionUtils.isEmpty(userIds)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        
        long startTime = System.currentTimeMillis();
        List<User> userList = userService.getUsersByIdsWithCache(userIds);
        long endTime = System.currentTimeMillis();
        
        List<UserVO> userVOList = userList.stream().map(user -> {
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(user, userVO);
            return userVO;
        }).collect(Collectors.toList());
        
        // 添加查询统计信息
        Map<String, Object> extraInfo = new HashMap<>();
        extraInfo.put("queryTime", (endTime - startTime) + "ms");
        extraInfo.put("cacheHitCount", userList.size());
        extraInfo.put("totalCount", userIds.size());
        
        return ResultUtils.success(userVOList, extraInfo);
    }
} 
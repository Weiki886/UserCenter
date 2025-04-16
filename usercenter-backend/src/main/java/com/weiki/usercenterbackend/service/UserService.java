package com.weiki.usercenterbackend.service;

import com.weiki.usercenterbackend.model.domain.User;
import com.weiki.usercenterbackend.model.request.UserUpdateRequest;
import com.weiki.usercenterbackend.model.vo.PageVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 用户服务
 */
public interface UserService {

    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @param request       HTTP请求
     * @return 脱敏后的用户信息
     */
    User userRegister(String userAccount, String userPassword, String checkPassword, HttpServletRequest request);

    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request
     * @return 脱敏后的用户信息
     */
    User userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 用户脱敏
     *
     * @param originUser
     * @return
     */
    User getSafetyUser(User originUser);

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    int userLogout(HttpServletRequest request);

    /**
     * 根据ID获取用户
     * @param id
     * @return
     */
    User getById(long id);
    
    /**
     * 根据ID获取用户（优先从缓存获取）
     * @param id 用户ID
     * @return 用户信息
     */
    User getByIdWithCache(long id);
    
    /**
     * 批量获取用户信息
     * @param userIds 用户ID列表
     * @return 用户信息列表
     */
    List<User> getUsersByIds(List<Long> userIds);

    /**
     * 批量获取用户信息（优先从缓存获取）
     * @param userIds 用户ID列表
     * @return 用户信息列表
     */
    List<User> getUsersByIdsWithCache(List<Long> userIds);
    
    /**
     * 删除用户
     * @param id 用户id
     * @return 是否成功
     */
    boolean deleteUser(long id);
    
    /**
     * 更新用户信息
     * @param updateRequest 用户更新请求
     * @param request HTTP请求
     * @return 是否成功
     */
    boolean updateUser(UserUpdateRequest updateRequest, HttpServletRequest request);
    
    /**
     * 分页获取用户列表
     * @param current 当前页
     * @param pageSize 页面大小
     * @param username 用户名（可选，模糊查询）
     * @param userAccount 账号（可选，模糊查询）
     * @param userRole 用户角色（可选）
     * @return 分页用户数据
     */
    PageVO<User> getUserPage(long current, long pageSize, String username, String userAccount, Integer userRole);

    /**
     * 更新用户密码
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @param checkPassword 确认密码
     * @param request HTTP请求
     * @return 是否成功
     */
    boolean updatePassword(String oldPassword, String newPassword, String checkPassword, HttpServletRequest request);
    
    /**
     * 注销用户自己的账号
     * @param userAccount 用户账号
     * @param userPassword 用户密码
     * @param request HTTP请求
     * @return 是否成功
     */
    boolean deleteAccount(String userAccount, String userPassword, HttpServletRequest request);
    
    /**
     * 封禁用户
     * @param userId 用户ID
     * @param banDays 封禁天数，0表示永久封禁
     * @param reason 封禁原因
     * @param request HTTP请求
     * @return 是否成功
     */
    boolean banUser(long userId, int banDays, String reason, HttpServletRequest request);
    
    /**
     * 解封用户
     * @param userId 用户ID
     * @param request HTTP请求
     * @return 是否成功
     */
    boolean unbanUser(long userId, HttpServletRequest request);
    
    /**
     * 获取封禁用户列表
     * @param current 当前页
     * @param pageSize 页面大小
     * @return 封禁用户数据
     */
    PageVO<User> getBannedUserPage(long current, long pageSize);
} 
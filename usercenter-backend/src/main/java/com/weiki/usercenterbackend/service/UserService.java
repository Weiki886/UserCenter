package com.weiki.usercenterbackend.service;

import com.weiki.usercenterbackend.model.domain.User;
import com.weiki.usercenterbackend.model.request.UserUpdateRequest;
import com.weiki.usercenterbackend.model.vo.PageVO;

import javax.servlet.http.HttpServletRequest;

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
} 
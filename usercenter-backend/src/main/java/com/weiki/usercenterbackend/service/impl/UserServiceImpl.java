package com.weiki.usercenterbackend.service.impl;

import com.weiki.usercenterbackend.common.ErrorCode;
import com.weiki.usercenterbackend.exception.BusinessException;
import com.weiki.usercenterbackend.mapper.UserMapper;
import com.weiki.usercenterbackend.model.domain.User;
import com.weiki.usercenterbackend.model.request.UserUpdateRequest;
import com.weiki.usercenterbackend.model.vo.PageVO;
import com.weiki.usercenterbackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.weiki.usercenterbackend.constant.UserConstant.DEFAULT_ROLE;
import static com.weiki.usercenterbackend.constant.UserConstant.USER_LOGIN_STATE;
import static com.weiki.usercenterbackend.constant.UserConstant.ADMIN_ROLE;

/**
 * 用户服务实现类
 */
@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Resource
    private UserMapper userMapper;

    /**
     * 盐值，混淆密码
     */
    private static final String SALT = "weiki";

    @Override
    public User userRegister(String userAccount, String userPassword, String checkPassword, HttpServletRequest request) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        // 账户不能包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号不能包含特殊字符");
        }
        // 密码和校验密码相同
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }
        
        // 检查账号是否已存在（未删除状态）
        User activeUser = userMapper.selectByUserAccount(userAccount);
        if (activeUser != null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号已存在");
        }
        
        // 检查账号是否已存在但被删除
        User deletedUser = userMapper.selectByUserAccountWithDeleted(userAccount);
        if (deletedUser != null && deletedUser.getIsDelete() == 1) {
            // 如果账号已被删除，则先物理删除该记录
            log.info("账号{}已被逻辑删除，执行物理删除", userAccount);
            userMapper.deletePhysically(deletedUser.getId());
        }

        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());

        // 3. 插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        // 设置用户状态，0表示正常
        user.setUserStatus(0);
        // 设置用户角色，默认为普通用户
        user.setUserRole(DEFAULT_ROLE);
        int saveResult = userMapper.insert(user);
        if (saveResult <= 0) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
        }

        // 4. 用户脱敏
        User safetyUser = getSafetyUser(user);
        
        // 5. 记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, safetyUser);
        
        return safetyUser;
    }

    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号错误");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
        }
        // 账户不能包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号包含特殊字符");
        }

        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 查询用户是否存在
        User user = userMapper.selectByUserAccount(userAccount);
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在");
        }
        if (!user.getUserPassword().equals(encryptPassword)) {
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
        }

        // 3. 用户脱敏
        User safetyUser = getSafetyUser(user);

        // 4. 记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, safetyUser);

        return safetyUser;
    }

    /**
     * 用户脱敏
     *
     * @param originUser
     * @return
     */
    @Override
    public User getSafetyUser(User originUser) {
        if (originUser == null) {
            return null;
        }
        User safetyUser = new User();
        safetyUser.setId(originUser.getId());
        safetyUser.setUsername(originUser.getUsername());
        safetyUser.setUserAccount(originUser.getUserAccount());
        safetyUser.setAvatarUrl(originUser.getAvatarUrl());
        safetyUser.setGender(originUser.getGender());
        safetyUser.setPhone(originUser.getPhone());
        safetyUser.setEmail(originUser.getEmail());
        safetyUser.setUserRole(originUser.getUserRole());
        safetyUser.setUserStatus(originUser.getUserStatus());
        safetyUser.setCreateTime(originUser.getCreateTime());
        return safetyUser;
    }

    /**
     * 用户注销
     *
     * @param request
     */
    @Override
    public int userLogout(HttpServletRequest request) {
        // 移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return 1;
    }

    @Override
    public User getById(long id) {
        return userMapper.selectById(id);
    }
    
    @Override
    public boolean deleteUser(long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户ID不合法");
        }
        // 逻辑删除
        int result = userMapper.deleteById(id);
        return result > 0;
    }

    @Override
    public boolean updateUser(UserUpdateRequest updateRequest, HttpServletRequest request) {
        if (updateRequest == null || request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        
        // 添加调试日志
        log.info("更新用户请求: {}", updateRequest);
        
        // 获取当前登录用户
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        
        log.info("当前登录用户: {}, 角色: {}", currentUser.getUserAccount(), currentUser.getUserRole());
        
        // 获取要修改的用户ID - 无论是否是管理员，都只能修改自己的信息
        Long userId = currentUser.getId();
        log.info("用户修改自己的信息, ID: {}", userId);
        
        // 如果请求中包含ID且与当前用户ID不一致，拒绝请求
        if (updateRequest.getId() != null && !updateRequest.getId().equals(userId)) {
            log.warn("用户尝试修改他人信息，拒绝请求");
            throw new BusinessException(ErrorCode.NO_AUTH, "只能修改自己的信息");
        }
        
        // 根据ID获取用户
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在");
        }
        
        // 更新用户信息
        if (updateRequest.getUsername() != null) {
            user.setUsername(updateRequest.getUsername());
        }
        if (updateRequest.getAvatarUrl() != null) {
            user.setAvatarUrl(updateRequest.getAvatarUrl());
        }
        if (updateRequest.getGender() != null) {
            user.setGender(updateRequest.getGender());
        }
        if (updateRequest.getPhone() != null) {
            user.setPhone(updateRequest.getPhone());
        }
        if (updateRequest.getEmail() != null) {
            user.setEmail(updateRequest.getEmail());
        }
        
        // 更新用户
        int result = userMapper.updateById(user);
        
        // 更新session中的用户信息
        if (result > 0) {
            User safetyUser = getSafetyUser(user);
            request.getSession().setAttribute(USER_LOGIN_STATE, safetyUser);
        }
        
        log.info("用户更新结果: {}", result > 0 ? "成功" : "失败");
        
        return result > 0;
    }

    @Override
    public PageVO<User> getUserPage(long current, long pageSize, String username, String userAccount, Integer userRole) {
        // 边界条件处理
        if (current < 1) {
            current = 1;
        }
        if (pageSize < 1 || pageSize > 100) {
            pageSize = 10;
        }
        
        // 计算偏移量
        int offset = (int) ((current - 1) * pageSize);
        
        // 查询数据
        List<User> userList = userMapper.listUsersByPage(username, userAccount, userRole, offset, (int) pageSize);
        
        // 查询总数
        long total = userMapper.countUsers(username, userAccount, userRole);
        
        // 用户脱敏处理
        List<User> safetyUserList = userList.stream()
                .map(this::getSafetyUser)
                .collect(Collectors.toList());
        
        // 封装结果
        return new PageVO<>(safetyUserList, total, current, pageSize);
    }
} 
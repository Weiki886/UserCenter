package com.weiki.usercenterbackend.service.impl;

import com.weiki.usercenterbackend.annotation.DistributedLock;
import com.weiki.usercenterbackend.common.ErrorCode;
import com.weiki.usercenterbackend.exception.BusinessException;
import com.weiki.usercenterbackend.mapper.UserMapper;
import com.weiki.usercenterbackend.model.domain.User;
import com.weiki.usercenterbackend.model.request.UserUpdateRequest;
import com.weiki.usercenterbackend.model.vo.PageVO;
import com.weiki.usercenterbackend.service.DistributedLockService;
import com.weiki.usercenterbackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.text.SimpleDateFormat;

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
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    
    @Autowired
    private DistributedLockService distributedLockService;
    
    // 用户缓存前缀
    private static final String USER_CACHE_KEY_PREFIX = "user:";
    
    // 用户缓存锁前缀
    private static final String USER_LOCK_KEY_PREFIX = "lock:user:";
    
    // 用户缓存默认过期时间（3600秒 = 1小时）
    private static final int USER_CACHE_EXPIRE_SECONDS = 3600;
    
    // 用户不存在标记，防止缓存穿透
    private static class NullUserMarker implements Serializable {
        private static final long serialVersionUID = 1L;
        // 内部空类
    }

    /**
     * 盐值，混淆密码
     */
    private static final String SALT = "weiki";

    /**
     * 根据用户账号生成默认用户名
     * 
     * @param userAccount 用户账号
     * @return 默认用户名
     */
    private String generateDefaultUsername(String userAccount) {
        return "用户_" + userAccount;
    }

    @Override
    @DistributedLock(lockKey = "'userRegister:' + #userAccount", waitTime = 5000, leaseTime = 30000)
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
            // 无论密码是否正确，都返回相同的错误信息和错误码
            throw new BusinessException("注册失败：账号已存在", 40000, "");
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
        // 设置默认用户名
        user.setUsername(generateDefaultUsername(userAccount));
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
            throw new BusinessException(ErrorCode.LOGIN_ERROR);
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.LOGIN_ERROR);
        }
        // 账户不能包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            throw new BusinessException(ErrorCode.LOGIN_ERROR);
        }

        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 查询用户是否存在
        User user = userMapper.selectByUserAccount(userAccount);
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.LOGIN_ERROR);
        }
        if (!user.getUserPassword().equals(encryptPassword)) {
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.LOGIN_ERROR);
        }
        
        // 检查用户是否为管理员，管理员不受封禁限制
        if (user.getUserRole() != ADMIN_ROLE) {
            // 检查用户是否被封禁
            if (user.getIsBanned() != null && user.getIsBanned() == 1) {
                Date now = new Date();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                
                // 检查是否永久封禁（unbanDate为null）
                if (user.getUnbanDate() == null) {
                    throw new BusinessException(ErrorCode.FORBIDDEN, "账号已被永久封禁，原因：" + user.getBanReason());
                }
                
                // 检查封禁是否已过期
                if (now.before(user.getUnbanDate())) {
                    // 封禁未过期
                    String formattedDate = dateFormat.format(user.getUnbanDate());
                    throw new BusinessException(ErrorCode.FORBIDDEN, 
                        "账号封禁至" + formattedDate + "，原因：" + user.getBanReason());
                } else {
                    // 封禁已过期，自动解封
                    user.setIsBanned(0);
                    user.setUnbanDate(null);
                    userMapper.updateById(user);
                }
            }
        } else {
            log.info("管理员登录，跳过封禁检查");
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
        safetyUser.setIsBanned(originUser.getIsBanned());
        safetyUser.setUnbanDate(originUser.getUnbanDate());
        safetyUser.setBanReason(originUser.getBanReason());
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
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "userCache", key = "#id")
    public boolean deleteUser(long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户ID不合法");
        }
        
        // 1. 逻辑删除
        int result = userMapper.deleteById(id);
        
        // 2. 清除缓存
        String cacheKey = USER_CACHE_KEY_PREFIX + id;
        redisTemplate.delete(cacheKey);
        
        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
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
        
        // 要修改的用户ID
        Long userId;
        
        // 管理员可以修改任何用户信息
        boolean isAdmin = currentUser.getUserRole() == ADMIN_ROLE;
        
        if (isAdmin && updateRequest.getId() != null) {
            // 如果是管理员且指定了用户ID，则修改指定用户的信息
            userId = updateRequest.getId();
            log.info("管理员修改用户信息, 目标用户ID: {}", userId);
        } else {
            // 如果不是管理员，或者没有指定用户ID，则只能修改自己的信息
            userId = currentUser.getId();
            log.info("用户修改自己的信息, ID: {}", userId);
            
            // 如果非管理员尝试修改他人信息，拒绝请求
            if (updateRequest.getId() != null && !updateRequest.getId().equals(userId)) {
                log.warn("非管理员用户尝试修改他人信息，拒绝请求");
                throw new BusinessException(ErrorCode.NO_AUTH, "只能修改自己的信息");
            }
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
        
        // 如果是修改当前登录用户的信息，更新session中的用户信息
        if (result > 0 && userId.equals(currentUser.getId())) {
            User safetyUser = getSafetyUser(user);
            request.getSession().setAttribute(USER_LOGIN_STATE, safetyUser);
        }
        
        // 清除缓存
        String cacheKey = USER_CACHE_KEY_PREFIX + userId;
        redisTemplate.delete(cacheKey);
        log.info("用户信息更新，已清除缓存，userId={}", userId);
        
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

    @Override
    @DistributedLock(lockKey = "'updatePassword:' + #request.getSession().getAttribute('USER_LOGIN_STATE').id", waitTime = 5000, leaseTime = 30000)
    public boolean updatePassword(String oldPassword, String newPassword, String checkPassword, HttpServletRequest request) {
        // 校验参数
        if (StringUtils.isAnyBlank(oldPassword, newPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (newPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度不能小于8位");
        }
        // 新密码和确认密码相同
        if (!newPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的新密码不一致");
        }
        
        // 获取当前登录用户
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        
        // 查询用户信息
        Long userId = currentUser.getId();
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在");
        }
        
        // 校验旧密码
        String encryptOldPassword = DigestUtils.md5DigestAsHex((SALT + oldPassword).getBytes());
        if (!encryptOldPassword.equals(user.getUserPassword())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "旧密码错误");
        }
        
        // 加密新密码
        String encryptNewPassword = DigestUtils.md5DigestAsHex((SALT + newPassword).getBytes());
        
        // 更新用户密码
        user.setUserPassword(encryptNewPassword);
        int result = userMapper.updateById(user);
        
        // 清除登录状态，让用户重新登录
        if (result > 0) {
            request.getSession().removeAttribute(USER_LOGIN_STATE);
        }
        
        return result > 0;
    }

    /**
     * 封禁用户
     *
     * @param userId  用户ID
     * @param banDays 封禁天数，0表示永久封禁
     * @param reason  封禁原因
     * @param request HTTP请求
     * @return 是否成功
     */
    @Override
    public boolean banUser(long userId, int banDays, String reason, HttpServletRequest request) {
        // 校验封禁原因
        if (StringUtils.isBlank(reason)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "封禁原因不能为空");
        }
        
        // 获取要封禁的用户
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在");
        }
        
        // 检查用户是否为管理员，管理员不能被封禁
        if (user.getUserRole() != null && user.getUserRole() == 1) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "管理员无法被封禁");
        }
        
        // 检查用户是否已被封禁
        if (user.getIsBanned() != null && user.getIsBanned() == 1) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "用户已被封禁");
        }
        
        // 记录用户当前状态用于日志，特别是先前的封禁/解封状态
        log.info("封禁前用户状态检查 - 用户ID: {}, 当前封禁状态: {}, 当前unbanDate: {}", 
                userId, user.getIsBanned(), user.getUnbanDate());
        
        // 用于日志的变量
        boolean isPermanentBan = banDays == 0;
        Date unbanDate = null;
        int result;
        
        if (isPermanentBan) {
            // 永久封禁 - 使用专门的方法确保unbanDate为null
            log.info("执行永久封禁，强制清除可能存在的历史unbanDate记录");
            result = userMapper.permanentBanUser(userId, reason);
        } else {
            // 临时封禁 - 使用常规方法设置unbanDate
            // 设置封禁状态
            user.setIsBanned(1);
            user.setBanReason(reason);
            
            // 计算解封日期
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, banDays);
            unbanDate = calendar.getTime();
            user.setUnbanDate(unbanDate);
            
            log.info("设置临时封禁，解封日期: {}", unbanDate);
            result = userMapper.updateById(user);
        }
        
        // 记录操作日志
        log.info("用户 {} 被{}封禁，封禁天数：{}，原因：{}, 解封日期：{}, 更新结果: {}",
                userId, isPermanentBan ? "永久" : "临时", banDays, reason, 
                isPermanentBan ? "永不解封" : unbanDate, result > 0 ? "成功" : "失败");
        
        // 如果此用户有缓存，强制清除
        String cacheKey = USER_CACHE_KEY_PREFIX + userId;
        try {
            boolean hasKey = redisTemplate.hasKey(cacheKey);
            if (hasKey) {
                redisTemplate.delete(cacheKey);
                log.info("已清除用户{}的缓存数据", userId);
            }
        } catch (Exception e) {
            log.warn("清除用户缓存出错", e);
        }
        
        return result > 0;
    }
    
    /**
     * 解封用户
     *
     * @param userId  用户ID
     * @param request HTTP请求
     * @return 是否成功
     */
    @Override
    public boolean unbanUser(long userId, HttpServletRequest request) {
        // 获取要解封的用户
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在");
        }
        
        // 检查用户是否已被封禁
        if (user.getIsBanned() == null || user.getIsBanned() != 1) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "用户未被封禁");
        }
        
        // 记录用户解封前的状态
        boolean wasPermanentBan = user.getUnbanDate() == null;
        log.info("解封前用户状态 - 用户ID: {}, 封禁状态: {}, 解封日期: {}, 是否永久封禁: {}, 封禁原因: {}", 
                userId, user.getIsBanned(), user.getUnbanDate(), wasPermanentBan, user.getBanReason());
        
        // 彻底清除所有封禁相关数据
        user.setIsBanned(0);       // 设置为未封禁
        user.setUnbanDate(null);   // 清除解封日期
        user.setBanReason(null);   // 清除封禁原因
        
        // 更新用户
        int result = userMapper.updateById(user);
        
        // 记录操作日志
        log.info("用户 {} 被解封，原封禁类型: {}, 更新结果: {}", 
                userId, wasPermanentBan ? "永久封禁" : "临时封禁", result > 0 ? "成功" : "失败");
        
        // 强制清除用户缓存
        String cacheKey = USER_CACHE_KEY_PREFIX + userId;
        try {
            boolean hasKey = redisTemplate.hasKey(cacheKey);
            if (hasKey) {
                redisTemplate.delete(cacheKey);
                log.info("已清除用户{}的缓存数据", userId);
            }
        } catch (Exception e) {
            log.warn("清除用户缓存出错", e);
        }
        
        return result > 0;
    }
    
    /**
     * 获取封禁用户列表
     *
     * @param current  当前页
     * @param pageSize 页面大小
     * @return 封禁用户数据
     */
    @Override
    public PageVO<User> getBannedUserPage(long current, long pageSize) {
        // 边界条件处理
        if (current < 1) {
            current = 1;
        }
        if (pageSize < 1 || pageSize > 100) {
            pageSize = 10;
        }
        
        // 计算偏移量
        int offset = (int) ((current - 1) * pageSize);
        
        // 查询被封禁的用户数据
        List<User> userList = userMapper.listBannedUsersByPage(offset, (int) pageSize);
        
        // 查询总数
        long total = userMapper.countBannedUsers();
        
        // 用户脱敏处理
        List<User> safetyUserList = userList.stream()
                .map(this::getSafetyUser)
                .collect(Collectors.toList());
        
        // 封装结果
        return new PageVO<>(safetyUserList, total, current, pageSize);
    }

    /**
     * 根据用户ID获取用户信息（加入Redis缓存）
     *
     * @param id 用户ID
     * @return 用户信息
     */
    @Override
    @Cacheable(value = "userCache", key = "#id", unless = "#result == null")
    public User getByIdWithCache(long id) {
        if (id <= 0) {
            return null;
        }
        
        String cacheKey = USER_CACHE_KEY_PREFIX + id;
        
        // 1. 尝试从Redis缓存中获取
        User cachedUser = (User) redisTemplate.opsForValue().get(cacheKey);
        if (cachedUser != null) {
            log.info("用户缓存命中，userId={}", id);
            return cachedUser;
        }
        
        // 如果获取到的是NullUserMarker，表示该用户不存在
        if (cachedUser != null && cachedUser.getClass().equals(NullUserMarker.class)) {
            log.info("用户缓存命中空值，userId={}", id);
            return null;
        }
        
        // 2. 缓存未命中，使用分布式锁防止缓存击穿
        String lockKey = USER_LOCK_KEY_PREFIX + id;
        boolean locked = false;
        
        try {
            locked = distributedLockService.tryLock(lockKey, 2000, 5000, TimeUnit.MILLISECONDS, false);
            
            if (locked) {
                // 双重检查，可能其他线程已加载过
                cachedUser = (User) redisTemplate.opsForValue().get(cacheKey);
                if (cachedUser != null) {
                    return cachedUser;
                }
                
                // 3. 查询数据库
                User user = userMapper.selectById(id);
                
                // 4. 写入缓存
                if (user != null) {
                    // 设置随机过期时间，防止缓存雪崩
                    int expireTime = USER_CACHE_EXPIRE_SECONDS + new Random().nextInt(300);
                    redisTemplate.opsForValue().set(cacheKey, user, expireTime, TimeUnit.SECONDS);
                    log.info("用户缓存已更新，userId={}, expireTime={}s", id, expireTime);
                    return user;
                } else {
                    // 缓存空值，防止缓存穿透，过期时间短一些
                    redisTemplate.opsForValue().set(cacheKey, new NullUserMarker(), 60, TimeUnit.SECONDS);
                    log.info("用户不存在，已缓存空标记，userId={}", id);
                    return null;
                }
            } else {
                // 获取锁失败，直接查数据库
                log.warn("获取用户缓存锁失败，直接查询数据库，userId={}", id);
                return userMapper.selectById(id);
            }
        } catch (Exception e) {
            log.error("获取用户缓存异常，userId={}, error={}", id, e.getMessage(), e);
            // 出现异常，降级处理，直接查数据库
            return userMapper.selectById(id);
        } finally {
            if (locked) {
                try {
                    distributedLockService.unlock(lockKey);
                } catch (Exception e) {
                    log.error("释放用户缓存锁异常，userId={}, error={}", id, e.getMessage(), e);
                }
            }
        }
    }
    
    /**
     * 批量获取用户信息
     * 
     * @param userIds 用户ID列表
     * @return 用户信息列表
     */
    @Override
    public List<User> getUsersByIds(List<Long> userIds) {
        if (CollectionUtils.isEmpty(userIds)) {
            return Collections.emptyList();
        }
        
        // 使用单个查询替代批量查询
        List<User> users = new ArrayList<>();
        for (Long userId : userIds) {
            User user = userMapper.selectById(userId);
            if (user != null) {
                users.add(user);
            }
        }
        return users;
    }
    
    /**
     * 批量获取用户信息，优先从缓存获取
     * 
     * @param userIds 用户ID列表
     * @return 用户信息列表
     */
    @Override
    public List<User> getUsersByIdsWithCache(List<Long> userIds) {
        if (CollectionUtils.isEmpty(userIds)) {
            return Collections.emptyList();
        }
        
        List<User> result = new ArrayList<>(userIds.size());
        List<Long> missedIds = new ArrayList<>();
        
        // 1. 尝试从缓存批量获取
        List<String> cacheKeys = userIds.stream()
                .map(id -> USER_CACHE_KEY_PREFIX + id)
                .collect(Collectors.toList());
        
        // 使用管道批量获取，减少网络往返
        List<Object> cachedUserList = redisTemplate.opsForValue().multiGet(cacheKeys);
        
        // 2. 组装结果，记录缓存未命中的ID
        if (cachedUserList != null) {
            for (int i = 0; i < userIds.size(); i++) {
                Object cachedUser = cachedUserList.get(i);
                if (cachedUser != null && !cachedUser.getClass().equals(NullUserMarker.class)) {
                    result.add((User) cachedUser);
                } else {
                    // 缓存未命中或为空值标记
                    missedIds.add(userIds.get(i));
                    result.add(null); // 占位，保持索引一致
                }
            }
        } else {
            // Redis可能出现异常，所有ID都视为未命中
            missedIds.addAll(userIds);
            for (int i = 0; i < userIds.size(); i++) {
                result.add(null);
            }
        }
        
        // 3. 处理缓存未命中的ID
        if (!missedIds.isEmpty()) {
            Map<Long, User> userMap = new HashMap<>();
            
            // 逐个查询替代批量查询
            for (Long missedId : missedIds) {
                User user = userMapper.selectById(missedId);
                if (user != null) {
                    userMap.put(user.getId(), user);
                }
            }
            
            // 4. 更新结果和缓存
            for (int i = 0; i < userIds.size(); i++) {
                Long userId = userIds.get(i);
                if (missedIds.contains(userId)) {
                    User dbUser = userMap.get(userId);
                    if (dbUser != null) {
                        // 更新结果
                        result.set(i, dbUser);
                        
                        // 更新缓存
                        String cacheKey = USER_CACHE_KEY_PREFIX + userId;
                        int expireTime = USER_CACHE_EXPIRE_SECONDS + new Random().nextInt(300);
                        redisTemplate.opsForValue().set(cacheKey, dbUser, expireTime, TimeUnit.SECONDS);
                    } else {
                        // 缓存空值，防止缓存穿透
                        String cacheKey = USER_CACHE_KEY_PREFIX + userId;
                        redisTemplate.opsForValue().set(cacheKey, new NullUserMarker(), 60, TimeUnit.SECONDS);
                    }
                }
            }
        }
        
        // 5. 移除结果中的null值
        return result.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
    
    /**
     * 批量删除用户缓存
     * 
     * @param userIds 用户ID列表
     */
    public void batchDeleteUserCache(List<Long> userIds) {
        if (CollectionUtils.isEmpty(userIds)) {
            return;
        }
        
        List<String> cacheKeys = userIds.stream()
                .map(id -> USER_CACHE_KEY_PREFIX + id)
                .collect(Collectors.toList());
                
        redisTemplate.delete(cacheKeys);
        log.info("批量删除用户缓存，userIds={}", userIds);
    }
} 
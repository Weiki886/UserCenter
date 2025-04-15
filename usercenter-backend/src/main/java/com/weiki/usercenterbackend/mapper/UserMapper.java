package com.weiki.usercenterbackend.mapper;

import com.weiki.usercenterbackend.model.domain.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 用户Mapper
 */
@Mapper
public interface UserMapper {

    /**
     * 根据ID查询用户
     * @param id
     * @return
     */
    User selectById(Long id);

    /**
     * 根据账号查询用户
     * @param userAccount
     * @return
     */
    User selectByUserAccount(String userAccount);

    /**
     * 根据账号查询用户（包括已删除的用户）
     * @param userAccount
     * @return
     */
    User selectByUserAccountWithDeleted(String userAccount);

    /**
     * 物理删除用户（用于允许重新注册已删除的账号）
     * @param id
     * @return
     */
    int deletePhysically(Long id);

    /**
     * 插入用户
     * @param user
     * @return
     */
    int insert(User user);

    /**
     * 更新用户
     * @param user
     * @return
     */
    int updateById(User user);

    /**
     * 删除用户
     * @param id
     * @return
     */
    int deleteById(Long id);

    /**
     * 分页获取用户列表
     * @param username 用户名（模糊查询）
     * @param userAccount 用户账号（模糊查询）
     * @param userRole 用户角色
     * @param offset 偏移量
     * @param pageSize 页面大小
     * @return 用户列表
     */
    List<User> listUsersByPage(String username, String userAccount, Integer userRole, int offset, Integer pageSize);

    /**
     * 获取用户总数
     * @param username 用户名（模糊查询）
     * @param userAccount 用户账号（模糊查询）
     * @param userRole 用户角色
     * @return 用户总数
     */
    long countUsers(String username, String userAccount, Integer userRole);

    /**
     * 分页获取封禁用户列表
     * @param offset 偏移量
     * @param pageSize 页面大小
     * @return 封禁用户列表
     */
    List<User> listBannedUsersByPage(int offset, Integer pageSize);

    /**
     * 获取封禁用户总数
     * @return 封禁用户总数
     */
    long countBannedUsers();

    /**
     * 永久封禁用户（强制设置unbanDate为null）
     * @param userId 用户ID
     * @param reason 封禁原因
     * @return 影响的行数
     */
    int permanentBanUser(Long userId, String reason);
} 
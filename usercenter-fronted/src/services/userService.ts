import api from './api';
import { AxiosResponse } from 'axios';

export interface UserLoginParams {
  userAccount: string;
  userPassword: string;
}

export interface UserRegisterParams {
  userAccount: string;
  userPassword: string;
  checkPassword: string;
}

export interface UserType {
  id: number;
  username: string;
  userAccount: string;
  avatarUrl?: string;
  gender?: number;
  phone?: string;
  email?: string;
  userRole: number;
  userStatus: number;
  isBanned?: number;
  unbanDate?: string;
  banReason?: string;
  createTime: Date;
}

export interface BaseResponse<T> {
  code: number;
  data: T;
  message: string;
  description: string;
}

export interface UserUpdateParams {
  id?: number;
  username?: string;
  avatarUrl?: string;
  gender?: number;
  phone?: string;
  email?: string;
}

export interface PageVO<T> {
  records: T[];
  total: number;
  current: number;
  pageSize: number;
}

export interface UserPageParams {
  current?: number;
  pageSize?: number;
  username?: string;
  userAccount?: string;
  userRole?: number;
}

export interface PasswordUpdateParams {
  oldPassword: string;
  newPassword: string;
  checkPassword: string;
}

export interface UserBanParams {
  userId: number;
  banDays: number;
  reason: string;
}

/**
 * 用户登录
 * @param params
 */
export async function login(params: UserLoginParams): Promise<UserType> {
  const response = await api.post<BaseResponse<UserType>>('/user/login', params);
  const { code, data, message, description } = response.data;
  
  if (code !== 0) {
    throw new Error(description || message || '登录失败');
  }
  
  return data;
}

/**
 * 用户注册
 * @param params
 */
export async function register(params: UserRegisterParams): Promise<UserType> {
  const response = await api.post<BaseResponse<UserType>>('/user/register', params);
  const { code, data, message, description } = response.data;
  
  if (code !== 0) {
    throw new Error(description || message || '注册失败');
  }
  
  return data;
}

/**
 * 用户注销
 */
export async function logout(): Promise<number> {
  const response = await api.post<BaseResponse<number>>('/user/logout');
  const { code, data, message, description } = response.data;
  
  if (code !== 0) {
    throw new Error(description || message || '注销失败');
  }
  
  // 清除所有用户相关的缓存
  api.invalidateCache('/user/current');
  api.invalidateCache('/user/list');
  
  return data;
}

/**
 * 获取当前用户信息
 */
export async function getCurrentUser(): Promise<UserType> {
  const response = await api.getCached<BaseResponse<UserType>>('/user/current');
  const { code, data, message, description } = response.data;
  
  if (code !== 0) {
    throw new Error(description || message || '获取用户信息失败');
  }
  
  return data;
}

/**
 * 刷新当前用户信息（强制绕过缓存）
 */
export async function refreshCurrentUser(): Promise<UserType> {
  // 先清除缓存
  api.invalidateCache('/user/current');
  return getCurrentUser();
}

/**
 * 删除用户 (仅管理员)
 * @param id 
 */
export async function deleteUser(id: number): Promise<boolean> {
  const response = await api.post<BaseResponse<boolean>>('/user/delete', { id });
  const { code, data, message, description } = response.data;
  
  if (code !== 0) {
    throw new Error(description || message || '删除用户失败');
  }
  
  // 删除用户后清除用户列表缓存
  api.invalidateCache('/user/list');
  return data;
}

/**
 * 更新用户信息
 * @param params 
 */
export async function updateUser(params: UserUpdateParams): Promise<UserType> {
  try {
    const response = await api.post<BaseResponse<UserType>>('/user/update', params);
    const { code, data, message, description } = response.data;
    
    if (code !== 0) {
      throw new Error(description || message || '更新用户信息失败');
    }
    
    // 如果更新的是当前登录用户，清除用户信息缓存
    if (params.id) {
      api.invalidateCache('/user/current');
    }
    
    // 清除用户列表缓存
    api.invalidateCache('/user/list');
    
    return data;
  } catch (error) {
    console.error('更新用户信息失败:', error);
    throw error;
  }
}

/**
 * 分页获取用户列表（仅管理员）
 * @param params 
 */
export async function getUserPage(params: UserPageParams): Promise<PageVO<UserType>> {
  const response = await api.get<BaseResponse<PageVO<UserType>>>('/user/list/page', { params });
  const { code, data, message, description } = response.data;
  
  if (code !== 0) {
    throw new Error(description || message || '获取用户列表失败');
  }
  
  return data;
}

/**
 * 修改用户密码
 * @param params 
 */
export async function updatePassword(params: PasswordUpdateParams): Promise<boolean> {
  try {
    const response = await api.post<BaseResponse<boolean>>('/user/update-password', params);
    const { code, data, message, description } = response.data;
    
    if (code !== 0) {
      throw new Error(description || message || '修改密码失败');
    }
    
    return data;
  } catch (error) {
    console.error('修改密码失败:', error);
    throw error;
  }
}

/**
 * 封禁用户（仅管理员）
 * @param params 
 */
export async function banUser(params: UserBanParams): Promise<boolean> {
  try {
    const response = await api.post<BaseResponse<boolean>>('/user/ban/ban', params);
    const { code, data, message, description } = response.data;
    
    if (code !== 0) {
      throw new Error(description || message || '封禁用户失败');
    }
    
    // 清除用户列表和封禁用户列表缓存
    api.invalidateCache('/user/list');
    api.invalidateCache('/user/ban/list');
    
    return data;
  } catch (error) {
    console.error('封禁用户失败:', error);
    throw error;
  }
}

/**
 * 解封用户（仅管理员）
 * @param userId 
 */
export async function unbanUser(userId: number): Promise<boolean> {
  try {
    const response = await api.post<BaseResponse<boolean>>('/user/ban/unban', { userId });
    const { code, data, message, description } = response.data;
    
    if (code !== 0) {
      throw new Error(description || message || '解封用户失败');
    }
    
    // 清除用户列表和封禁用户列表缓存
    api.invalidateCache('/user/list');
    api.invalidateCache('/user/ban/list');
    
    return data;
  } catch (error) {
    console.error('解封用户失败:', error);
    throw error;
  }
}

/**
 * 获取封禁用户列表（仅管理员）
 * @param current 
 * @param pageSize 
 */
export async function getBannedUsers(current: number = 1, pageSize: number = 10): Promise<PageVO<UserType>> {
  try {
    const response = await api.get<BaseResponse<PageVO<UserType>>>('/user/ban/list', { 
      params: { current, pageSize } 
    });
    const { code, data, message, description } = response.data;
    
    if (code !== 0) {
      throw new Error(description || message || '获取封禁用户列表失败');
    }
    
    return data;
  } catch (error) {
    console.error('获取封禁用户列表失败:', error);
    throw error;
  }
} 
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
  
  return data;
}

/**
 * 获取当前用户信息
 */
export async function getCurrentUser(): Promise<UserType> {
  const response = await api.get<BaseResponse<UserType>>('/user/current');
  const { code, data, message, description } = response.data;
  
  if (code !== 0) {
    throw new Error(description || message || '获取用户信息失败');
  }
  
  return data;
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
  
  return data;
}

/**
 * 更新用户信息
 * @param params 
 */
export async function updateUser(params: UserUpdateParams): Promise<boolean> {
  try {
    console.log('Enviando solicitud de actualización:', params);
    const response = await api.post<BaseResponse<boolean>>('/user/update', params);
    console.log('Respuesta del servidor:', response.data);
    const { code, data, message, description } = response.data;
    
    if (code !== 0) {
      console.error('Error al actualizar usuario:', { code, message, description });
      throw new Error(description || message || '更新用户信息失败');
    }
    
    return data;
  } catch (error) {
    console.error('Error en la llamada updateUser:', error);
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
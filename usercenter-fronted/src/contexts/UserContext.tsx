'use client';

import React, { createContext, useState, useContext, useEffect, ReactNode, useMemo, useCallback } from 'react';
import { UserType, getCurrentUser, refreshCurrentUser } from '@/services/userService';
import api from '@/services/api';

// 定义Context数据结构
interface UserContextType {
  currentUser: UserType | null;
  loading: boolean;
  refreshUserInfo: (silent?: boolean) => Promise<UserType | null>;
  clearUserInfo: () => void; 
  forceUpdate: () => void; // 新增强制更新方法
}

// 创建Context
const UserContext = createContext<UserContextType>({
  currentUser: null,
  loading: true,
  refreshUserInfo: async () => null,
  clearUserInfo: () => {},
  forceUpdate: () => {},
});

// 创建Provider组件
export function UserProvider({ children }: { children: ReactNode }) {
  // 初始化状态为null，避免服务端渲染时尝试读取localStorage
  const [currentUser, setCurrentUser] = useState<UserType | null>(null);
  const [loading, setLoading] = useState(true);
  const [isClient, setIsClient] = useState(false);
  const [updateKey, setUpdateKey] = useState(0); // 用于强制更新的key

  // 强制更新方法
  const forceUpdate = useCallback(() => {
    setUpdateKey(prev => prev + 1);
  }, []);

  // 确保只在客户端执行localStorage相关操作
  useEffect(() => {
    setIsClient(true);
    
    // 检查是否有userToken，没有则不应尝试加载用户信息
    const userToken = localStorage.getItem('userToken');
    
    // 添加检查token有效期的逻辑
    if (userToken) {
      try {
        // 检查token是否有效（这里可以添加解析JWT的逻辑，或者直接尝试刷新用户信息）
        // 如果项目首次启动，直接清除之前的登录状态
        const isFirstStart = sessionStorage.getItem('appStarted') !== 'true';
        if (isFirstStart) {
          console.log('首次启动应用，清除之前的登录状态');
          localStorage.removeItem('userToken');
          localStorage.removeItem('userInfo');
          sessionStorage.setItem('appStarted', 'true');
          setLoading(false);
          return;
        }
      } catch (e) {
        console.error('检查token有效期失败', e);
        localStorage.removeItem('userToken');
        localStorage.removeItem('userInfo');
        setLoading(false);
        return;
      }
    } else {
      setLoading(false);
      return;
    }
    
    // 尝试从localStorage获取用户信息
    try {
      const storedUser = localStorage.getItem('userInfo');
      if (storedUser) {
        const parsedUser = JSON.parse(storedUser);
        // 验证用户信息的有效性，确保至少包含必要的字段
        if (isValidUserInfo(parsedUser)) {
          setCurrentUser(parsedUser);
          setLoading(false);
        } else {
          // 如果无效，则清除存储的信息
          localStorage.removeItem('userInfo');
        }
      }
    } catch (e) {
      console.error('解析存储的用户信息失败', e);
      localStorage.removeItem('userInfo');
    }
    
    // 然后再获取最新数据
    fetchUserData();
  }, [updateKey]); // 添加updateKey作为依赖项

  // 验证用户信息是否有效的辅助函数
  const isValidUserInfo = (user: any): user is UserType => {
    return user 
      && typeof user === 'object'
      && typeof user.id === 'number'
      && typeof user.userAccount === 'string'
      && typeof user.userRole === 'number'
      && user.userRole >= 0; // 确保角色值有效
  };

  // 清除用户信息方法
  const clearUserInfo = useCallback(() => {
    // 清空当前用户
    setCurrentUser(null);
    
    // 同时清除localStorage中的数据
    if (typeof window !== 'undefined') {
      localStorage.removeItem('userToken');
      localStorage.removeItem('userInfo');
    }
    
    // 清除API缓存，确保下次请求会从服务器刷新
    api.invalidateCache('/user/current');
    api.invalidateCache('/user/list/page');
    
    // 强制更新
    forceUpdate();
  }, [forceUpdate]);

  // 使用useCallback优化刷新用户信息的方法，添加silent参数支持静默刷新
  const refreshUserInfo = useCallback(async (silent: boolean = false) => {
    try {
      if (!silent) setLoading(true);
      
      // 首先检查是否有token，如果没有则不应尝试获取用户信息
      const userToken = localStorage.getItem('userToken');
      if (!userToken) {
        setCurrentUser(null);
        return null;
      }
      
      // 确保先清除api缓存
      await refreshCurrentUser(); // 使用不带缓存的刷新
      
      // 重新获取用户信息
      const user = await getCurrentUser();
      
      if (user && isValidUserInfo(user)) {
        // 确保状态更新并保存到localStorage
        setCurrentUser(user as UserType);
        if (typeof window !== 'undefined') {
          localStorage.setItem('userInfo', JSON.stringify(user));
        }
        // 强制更新UI
        forceUpdate();
        return user;
      }
      
      return null; // 如果用户信息无效，返回null
    } catch (error) {
      console.error('获取用户信息失败:', error);
      // 如果获取用户信息失败，视为用户已登出
      setCurrentUser(null);
      if (typeof window !== 'undefined') {
        localStorage.removeItem('userInfo');
        localStorage.removeItem('userToken');
      }
      return null;
    } finally {
      if (!silent) setLoading(false);
    }
  }, [forceUpdate]);

  // 使用缓存版本获取用户信息
  async function fetchUserData() {
    try {
      // 首先检查是否有token，如果没有则不应尝试获取用户信息
      const userToken = localStorage.getItem('userToken');
      if (!userToken) {
        setCurrentUser(null);
        setLoading(false);
        return;
      }
      
      // 保持loading状态，直到用户信息获取完成
      setLoading(true);
      
      const user = await getCurrentUser();
      if (user && isValidUserInfo(user)) {
        setCurrentUser(user as UserType);
        if (typeof window !== 'undefined') {
          localStorage.setItem('userInfo', JSON.stringify(user));
        }
      } else {
        // 如果获取的用户信息无效，清除状态
        setCurrentUser(null);
        if (typeof window !== 'undefined') {
          localStorage.removeItem('userInfo');
          localStorage.removeItem('userToken');
        }
      }
    } catch (error) {
      console.error('获取用户信息失败:', error);
      setCurrentUser(null);
      if (typeof window !== 'undefined') {
        localStorage.removeItem('userInfo');
        localStorage.removeItem('userToken');
      }
    } finally {
      setLoading(false);
    }
  }

  // 定期静默刷新用户信息
  useEffect(() => {
    // 如果用户已登录，每5分钟静默刷新一次用户信息
    if (currentUser) {
      const refreshInterval = setInterval(() => {
        refreshUserInfo(true); // 静默刷新
      }, 5 * 60 * 1000);
      
      return () => clearInterval(refreshInterval);
    }
  }, [currentUser, refreshUserInfo]);

  // 使用useMemo优化context值，避免不必要的重新渲染
  const contextValue = useMemo(() => ({
    currentUser,
    loading,
    refreshUserInfo,
    clearUserInfo,
    forceUpdate,
  }), [currentUser, loading, refreshUserInfo, clearUserInfo, forceUpdate]);

  return (
    <UserContext.Provider value={contextValue}>
      {children}
    </UserContext.Provider>
  );
}

// 自定义Hook便于组件使用Context
export const useUser = () => useContext(UserContext); 
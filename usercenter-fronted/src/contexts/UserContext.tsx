'use client';

import React, { createContext, useState, useContext, useEffect, ReactNode, useMemo, useCallback } from 'react';
import { UserType, getCurrentUser, refreshCurrentUser } from '@/services/userService';
import api from '@/services/api';

// 定义Context数据结构
interface UserContextType {
  currentUser: UserType | null;
  loading: boolean;
  refreshUserInfo: (silent?: boolean) => Promise<UserType | null>;
  clearUserInfo: () => void; // 新增清除用户信息的方法
}

// 创建Context
const UserContext = createContext<UserContextType>({
  currentUser: null,
  loading: true,
  refreshUserInfo: async () => null,
  clearUserInfo: () => {},
});

// 创建Provider组件
export function UserProvider({ children }: { children: ReactNode }) {
  // 初始化状态为null，避免服务端渲染时尝试读取localStorage
  const [currentUser, setCurrentUser] = useState<UserType | null>(null);
  const [loading, setLoading] = useState(true);
  const [isClient, setIsClient] = useState(false);

  // 确保只在客户端执行localStorage相关操作
  useEffect(() => {
    setIsClient(true);
    // 尝试从localStorage获取用户信息
    try {
      const storedUser = localStorage.getItem('userInfo');
      if (storedUser) {
        const parsedUser = JSON.parse(storedUser);
        setCurrentUser(parsedUser);
        setLoading(false);
      }
    } catch (e) {
      console.error('解析存储的用户信息失败', e);
      localStorage.removeItem('userInfo');
    }
    
    // 然后再获取最新数据
    fetchUserData();
  }, []);

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
  }, []);

  // 使用useCallback优化刷新用户信息的方法，添加silent参数支持静默刷新
  const refreshUserInfo = useCallback(async (silent: boolean = false) => {
    try {
      if (!silent) setLoading(true);
      
      // 确保先清除api缓存
      await refreshCurrentUser(); // 使用不带缓存的刷新
      
      // 重新获取用户信息
      const user = await getCurrentUser();
      
      if (user) {
        // 确保状态更新并保存到localStorage
        setCurrentUser(user as UserType);
        if (typeof window !== 'undefined') {
          localStorage.setItem('userInfo', JSON.stringify(user));
        }
      }
      
      return user; // 返回用户信息便于后续操作
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
  }, []);

  // 使用缓存版本获取用户信息
  async function fetchUserData() {
    try {
      // 保持loading状态，直到用户信息获取完成
      setLoading(true);
      const user = await getCurrentUser();
      if (user) {
        setCurrentUser(user as UserType);
        if (typeof window !== 'undefined') {
          localStorage.setItem('userInfo', JSON.stringify(user));
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
  }), [currentUser, loading, refreshUserInfo, clearUserInfo]);

  return (
    <UserContext.Provider value={contextValue}>
      {children}
    </UserContext.Provider>
  );
}

// 自定义Hook便于组件使用Context
export const useUser = () => useContext(UserContext); 
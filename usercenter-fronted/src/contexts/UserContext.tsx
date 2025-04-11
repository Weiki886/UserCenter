'use client';

import React, { createContext, useState, useContext, useEffect, ReactNode, useMemo, useCallback } from 'react';
import { UserType, getCurrentUser, refreshCurrentUser } from '@/services/userService';

// 定义Context数据结构
interface UserContextType {
  currentUser: UserType | null;
  loading: boolean;
  refreshUserInfo: (silent?: boolean) => Promise<void>;
  clearUserInfo: () => void; // 新增清除用户信息的方法
}

// 创建Context
const UserContext = createContext<UserContextType>({
  currentUser: null,
  loading: true,
  refreshUserInfo: async () => {},
  clearUserInfo: () => {},
});

// 创建Provider组件
export function UserProvider({ children }: { children: ReactNode }) {
  const [currentUser, setCurrentUser] = useState<UserType | null>(null);
  const [loading, setLoading] = useState(true);

  // 清除用户信息方法
  const clearUserInfo = useCallback(() => {
    setCurrentUser(null);
  }, []);

  // 使用useCallback优化刷新用户信息的方法，添加silent参数支持静默刷新
  const refreshUserInfo = useCallback(async (silent: boolean = false) => {
    try {
      if (!silent) setLoading(true);
      const user = await refreshCurrentUser(); // 使用不带缓存的刷新
      if (user) {
        setCurrentUser(user as UserType);
      }
    } catch (error) {
      console.log('获取用户信息失败');
      // 如果获取用户信息失败，视为用户已登出
      setCurrentUser(null);
    } finally {
      if (!silent) setLoading(false);
    }
  }, []);

  // 组件挂载时获取用户信息
  useEffect(() => {
    // 使用缓存版本获取用户信息
    async function fetchUserData() {
      try {
        setLoading(true);
        const user = await getCurrentUser();
        if (user) {
          setCurrentUser(user as UserType);
        }
      } catch (error) {
        console.log('获取用户信息失败');
        setCurrentUser(null);
      } finally {
        setLoading(false);
      }
    }
    
    fetchUserData();
  }, []);
  
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
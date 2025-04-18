'use client';

import React, { createContext, useState, useContext, useEffect, ReactNode, useMemo, useCallback } from 'react';
import { UserType, getCurrentUser } from '@/services/userService';
import api from '@/services/api';

// 定义Context数据结构
interface UserContextType {
  currentUser: UserType | null;
  loading: boolean;
  refreshUserInfo: (silent?: boolean) => Promise<UserType | null>;
  clearUserInfo: () => void; 
  forceUpdate: () => void;
}

// 创建Context
const UserContext = createContext<UserContextType>({
  currentUser: null,
  loading: false, // 默认不显示加载状态
  refreshUserInfo: async () => null,
  clearUserInfo: () => {},
  forceUpdate: () => {},
});

// 创建Provider组件
export function UserProvider({ children }: { children: ReactNode }) {
  // 初始化状态
  const [currentUser, setCurrentUser] = useState<UserType | null>(null);
  const [loading, setLoading] = useState(false);
  const [isClient, setIsClient] = useState(false);
  const [updateKey, setUpdateKey] = useState(0);

  // 强制更新方法
  const forceUpdate = useCallback(() => {
    setUpdateKey(prev => prev + 1);
  }, []);

  // 验证用户信息是否有效的辅助函数
  const isValidUserInfo = useCallback((user: any): user is UserType => {
    return user 
      && typeof user === 'object'
      && typeof user.id === 'number'
      && typeof user.userAccount === 'string'
      && typeof user.userRole === 'number'
      && user.userRole >= 0;
  }, []);

  // 初始化用户信息 - 客户端渲染时执行
  useEffect(() => {
    setIsClient(true);
    
    try {
      // 尝试从localStorage获取用户信息
      const storedUser = localStorage.getItem('userInfo');
      const userToken = localStorage.getItem('userToken');
      
      // 如果没有token，无需处理
      if (!userToken) return;
      
      // 如果有本地用户信息，先使用它
      if (storedUser) {
        try {
          const parsedUser = JSON.parse(storedUser);
          if (isValidUserInfo(parsedUser)) {
            setCurrentUser(parsedUser);
          }
        } catch (e) {
          console.error('解析用户信息失败:', e);
        }
      }
      
      // 检查是否需要优先刷新用户信息
      const loginSuccess = localStorage.getItem('loginSuccess');
      if (loginSuccess === 'true' || !storedUser) {
        // 静默刷新用户信息
        fetchUserData(true, true);
      }
    } catch (e) {
      console.error('初始化用户数据失败:', e);
    }
  }, [isValidUserInfo]);

  // 清除用户信息方法
  const clearUserInfo = useCallback(() => {
    // 清空当前用户
    setCurrentUser(null);
    
    // 同时清除localStorage中的数据
    if (typeof window !== 'undefined') {
      localStorage.removeItem('userToken');
      localStorage.removeItem('userInfo');
      localStorage.removeItem('loginSuccess');
    }
    
    // 清除API缓存
    api.invalidateCache('/user/current');
    api.invalidateCache('/user/list/page');
  }, []);

  // 刷新用户信息方法
  const refreshUserInfo = useCallback(async (silent: boolean = false) => {
    try {
      // 仅在非静默模式时显示加载状态
      if (!silent) setLoading(true);
      
      // 首先检查是否有token
      const userToken = localStorage.getItem('userToken');
      if (!userToken) {
        setCurrentUser(null);
        return null;
      }
      
      // 清除API缓存
      api.invalidateCache('/user/current');
      
      // 获取最新用户信息
      const user = await getCurrentUser();
      
      if (user && isValidUserInfo(user)) {
        // 更新状态并保存到localStorage
        setCurrentUser(user);
        localStorage.setItem('userInfo', JSON.stringify(user));
        return user;
      }
      
      return null;
    } catch (error) {
      console.error('获取用户信息失败:', error);
      
      // 出错时不立即清除用户信息，保留上一次有效的状态
      // 只有明确得到无效用户信息时才清除
      return null;
    } finally {
      if (!silent) setLoading(false);
    }
  }, [isValidUserInfo]);

  // 使用缓存版本获取用户信息
  async function fetchUserData(forceFetch: boolean = false, silent: boolean = false) {
    try {
      // 检查token
      const userToken = localStorage.getItem('userToken');
      if (!userToken) {
        setCurrentUser(null);
        return;
      }
      
      // 仅在非静默模式时显示loading
      if (!silent) setLoading(true);
      
      // 强制刷新时清除缓存
      if (forceFetch) {
        api.invalidateCache('/user/current');
      }
      
      // 获取用户信息
      const user = await getCurrentUser();
      
      if (user && isValidUserInfo(user)) {
        setCurrentUser(user);
        localStorage.setItem('userInfo', JSON.stringify(user));
        
        // 完成后移除登录成功标志
        localStorage.removeItem('loginSuccess');
      } else {
        // 如果获取的用户信息无效但仍有token，不要立即清除状态
        // 避免因网络问题导致用户被登出
        console.warn('获取到无效的用户信息');
      }
    } catch (error) {
      console.error('获取用户信息失败:', error);
    } finally {
      if (!silent) setLoading(false);
    }
  }

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

// 创建自定义Hook
export const useUser = () => useContext(UserContext); 
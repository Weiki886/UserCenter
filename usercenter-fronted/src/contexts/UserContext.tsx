'use client';

import React, { createContext, useState, useContext, useEffect, ReactNode, useMemo, useCallback } from 'react';
import { UserType, getCurrentUser, refreshCurrentUser } from '@/services/userService';

// 定义Context数据结构
interface UserContextType {
  currentUser: UserType | null;
  loading: boolean;
  refreshUserInfo: () => Promise<void>;
}

// 创建Context
const UserContext = createContext<UserContextType>({
  currentUser: null,
  loading: true,
  refreshUserInfo: async () => {},
});

// 创建Provider组件
export function UserProvider({ children }: { children: ReactNode }) {
  const [currentUser, setCurrentUser] = useState<UserType | null>(null);
  const [loading, setLoading] = useState(true);

  // 使用useCallback优化刷新用户信息的方法
  const refreshUserInfo = useCallback(async () => {
    try {
      setLoading(true);
      const user = await refreshCurrentUser(); // 使用不带缓存的刷新
      if (user) {
        setCurrentUser(user as UserType);
      }
    } catch (error) {
      console.log('获取用户信息失败');
      setCurrentUser(null);
    } finally {
      setLoading(false);
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

  // 使用useMemo优化context值，避免不必要的重新渲染
  const contextValue = useMemo(() => ({
    currentUser,
    loading,
    refreshUserInfo
  }), [currentUser, loading, refreshUserInfo]);

  return (
    <UserContext.Provider value={contextValue}>
      {children}
    </UserContext.Provider>
  );
}

// 自定义Hook简化Context使用
export function useUser() {
  return useContext(UserContext);
} 
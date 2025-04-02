'use client';

import React, { createContext, useState, useContext, useEffect, ReactNode } from 'react';
import { UserType, getCurrentUser } from '@/services/userService';

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

  // 刷新用户信息的方法
  const refreshUserInfo = async () => {
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
  };

  // 组件挂载时获取用户信息
  useEffect(() => {
    refreshUserInfo();
  }, []);

  return (
    <UserContext.Provider value={{ currentUser, loading, refreshUserInfo }}>
      {children}
    </UserContext.Provider>
  );
}

// 自定义Hook简化Context使用
export function useUser() {
  return useContext(UserContext);
} 
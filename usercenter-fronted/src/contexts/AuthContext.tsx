import React, { createContext, useContext, useState, useEffect } from 'react';
import request from '../utils/request';
import { message } from 'antd';

interface AuthContextType {
  isAuthenticated: boolean;
  user: any | null;
  loading: boolean;
  login: (accessToken: string, refreshToken: string) => void;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [isAuthenticated, setIsAuthenticated] = useState<boolean>(false);
  const [user, setUser] = useState<any | null>(null);
  const [loading, setLoading] = useState<boolean>(true);

  useEffect(() => {
    // 检查本地存储中的token
    const token = localStorage.getItem('accessToken');
    if (token) {
      setIsAuthenticated(true);
      // 获取用户信息
      fetchUserInfo();
    } else {
      setLoading(false);
    }
  }, []);

  const fetchUserInfo = async () => {
    try {
      setLoading(true);
      const response = await request.get('/user/current');
      setUser(response.data);
    } catch (error) {
      console.error('Failed to fetch user info:', error);
      message.error('获取用户信息失败，请重新登录');
      // 如果获取用户信息失败，可能是token无效，执行登出操作
      logout();
    } finally {
      setLoading(false);
    }
  };

  const login = async (accessToken: string, refreshToken: string) => {
    try {
      setLoading(true);
      localStorage.setItem('accessToken', accessToken);
      localStorage.setItem('refreshToken', refreshToken);
      setIsAuthenticated(true);
      await fetchUserInfo();
    } catch (error) {
      console.error('Login failed:', error);
      message.error('登录失败，请重试');
      logout();
    }
  };

  const logout = () => {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    setIsAuthenticated(false);
    setUser(null);
    setLoading(false);
    window.location.href = '/login';
  };

  return (
    <AuthContext.Provider value={{ isAuthenticated, user, loading, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

export default AuthContext; 
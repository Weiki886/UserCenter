'use client';

import React, { useState, useEffect } from 'react';
import { Layout, message } from 'antd';
import { useRouter, usePathname } from 'next/navigation';
import { getCurrentUser, UserType } from '@/services/userService';
import NavBar from '@/components/NavBar';

const { Content, Footer } = Layout;

export default function DashboardLayout({ children }: { children: React.ReactNode }) {
  const [currentUser, setCurrentUser] = useState<UserType | null>(null);
  const router = useRouter();
  const pathname = usePathname();

  // 获取当前页面对应的导航项
  const getActiveNavItem = () => {
    if (pathname === '/dashboard') return 'dashboard';
    if (pathname === '/dashboard/settings') return 'settings';
    if (pathname === '/dashboard/users') return 'users';
    return '';
  };

  // 获取当前用户信息
  useEffect(() => {
    const fetchCurrentUser = async () => {
      try {
        const user = await getCurrentUser();
        if (user) {
          setCurrentUser(user as UserType);
        }
      } catch (error) {
        message.error('获取用户信息失败，请重新登录');
        router.push('/auth/login');
      }
    };

    fetchCurrentUser();
  }, [router]);

  return (
    <Layout style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column' }}>
      <NavBar activeItem={getActiveNavItem()} />
      
      <Content style={{ 
        flex: 1, 
        display: 'flex',
        flexDirection: 'column',
        padding: '0',
        background: '#f5f5f5'
      }}>
        <div style={{
          flex: 1,
          margin: '0',
          padding: '24px',
          background: '#fff',
        }}>
          {children}
        </div>
      </Content>
      
      <Footer style={{ 
        textAlign: 'center',
        background: '#fff',
        color: '#000',
        padding: '12px 0',
        borderTop: '1px solid #f0f0f0'
      }}>
        用户中心系统 ©{new Date().getFullYear()} by 凭君语未可
      </Footer>
    </Layout>
  );
} 
'use client';

import React from 'react';
import { Layout, message } from 'antd';
import { useRouter, usePathname } from 'next/navigation';
import NavBar from '@/components/NavBar';
import { useUser } from '@/contexts/UserContext';

const { Content, Footer } = Layout;

export default function DashboardLayout({ children }: { children: React.ReactNode }) {
  const router = useRouter();
  const pathname = usePathname();
  const { currentUser, loading } = useUser();

  // 获取当前页面对应的导航项
  const getActiveNavItem = () => {
    if (pathname === '/dashboard') return 'dashboard';
    if (pathname === '/dashboard/settings') return 'settings';
    if (pathname === '/dashboard/users') return 'users';
    return '';
  };

  // 检查用户登录状态并重定向
  React.useEffect(() => {
    if (!loading && !currentUser) {
      message.error('请先登录');
      router.push('/auth/login');
    }
  }, [currentUser, loading, router]);

  // 如果正在加载，可以显示加载状态
  if (loading) {
    return <div>加载中...</div>;
  }

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <NavBar activeItem={getActiveNavItem()} />
      <Content style={{ padding: '0 50px', marginTop: 64 }}>
        <div style={{ background: '#fff', padding: 24, minHeight: 380 }}>
          {children}
        </div>
      </Content>
      <Footer style={{ textAlign: 'center' }}>
        用户中心系统 ©{new Date().getFullYear()} Created by Weiki
      </Footer>
    </Layout>
  );
} 
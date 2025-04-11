'use client';

import React, { useState, useEffect } from 'react';
import { Button, Form, Input, message, Card, Divider, Spin } from 'antd';
import { UserOutlined, LockOutlined } from '@ant-design/icons';
import { login } from '@/services/userService';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { useUser } from '@/contexts/UserContext';

export default function LoginPage() {
  const [loading, setLoading] = useState(false);
  const [redirecting, setRedirecting] = useState(false);
  const router = useRouter();
  const { refreshUserInfo, currentUser, loading: userLoading } = useUser();

  // 当用户已登录时自动重定向到首页
  useEffect(() => {
    if (currentUser) {
      router.push('/');
    }
  }, [currentUser, router]);

  const onFinish = async (values: { userAccount: string; userPassword: string }) => {
    try {
      setLoading(true);
      
      // 1. 登录过程
      const userData = await login(values);
      message.success('登录成功！');
      
      // 2. 立即保存用户数据到localStorage (只在客户端执行)
      if (typeof window !== 'undefined') {
        localStorage.setItem('userInfo', JSON.stringify(userData));
      }
      
      // 3. 标记为重定向状态
      setRedirecting(true);
      
      // 4. 刷新用户信息并等待完成
      await refreshUserInfo();
      
      // 5. 使用replace而不是push，避免保留历史状态
      router.replace('/');
    } catch (error: any) {
      console.error('登录失败:', error);
      message.error(error.message || '登录失败，请重试');
      setRedirecting(false);
      setLoading(false);
    }
  };

  // 显示加载状态
  if (userLoading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
        <Spin size="large" tip="加载中..." />
      </div>
    );
  }

  // 显示重定向中状态
  if (redirecting) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
        <Spin size="large" tip="登录成功，正在跳转..." />
      </div>
    );
  }

  return (
    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh', background: '#f0f2f5' }}>
      <Card title="用户中心系统" style={{ width: 400 }}>
        <Form
          name="login"
          initialValues={{ remember: true }}
          onFinish={onFinish}
          autoComplete="off"
          size="large"
        >
          <Form.Item
            name="userAccount"
            rules={[{ required: true, message: '请输入用户账号!' }]}
          >
            <Input prefix={<UserOutlined />} placeholder="用户账号" />
          </Form.Item>

          <Form.Item
            name="userPassword"
            rules={[{ required: true, message: '请输入密码!' }]}
          >
            <Input.Password
              prefix={<LockOutlined />}
              placeholder="密码"
            />
          </Form.Item>

          <Form.Item>
            <Button type="primary" htmlType="submit" loading={loading} block>
              登录
            </Button>
          </Form.Item>
          
          <Divider />
          
          <div style={{ textAlign: 'center' }}>
            还没有账号？ <Link href="/auth/register" style={{ color: '#1890ff' }}>立即注册</Link>
          </div>
        </Form>
      </Card>
    </div>
  );
} 
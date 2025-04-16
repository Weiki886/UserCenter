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
  const { refreshUserInfo, currentUser, loading: userLoading, forceUpdate } = useUser();
  // 创建Message实例以避免被销毁
  const [messageApi, contextHolder] = message.useMessage();

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
      
      // 2. 立即保存用户数据到localStorage (只在客户端执行)
      if (typeof window !== 'undefined') {
        localStorage.setItem('userInfo', JSON.stringify(userData));
      }
      
      // 3. 标记为重定向状态
      setRedirecting(true);
      
      // 4. 刷新用户信息并等待完成
      await refreshUserInfo();
      
      // 5. 强制更新整个应用状态
      forceUpdate();
      
      // 6. 显示成功消息
      messageApi.success(`欢迎回来，${userData.username || userData.userAccount}！`);
      
      // 7. 短暂延迟后重定向，确保消息显示
      setTimeout(() => {
        // 使用replace而不是push，避免保留历史状态
        router.replace('/');
      }, 300);
    } catch (error: any) {
      console.error('登录失败:', error);
      // 显示错误消息
      messageApi.error(error.message || '登录失败，请稍后再试');
      setRedirecting(false);
    } finally {
      setLoading(false);
    }
  };

  // 显示加载状态
  if (userLoading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
        <Spin spinning={true} size="large">
          <div style={{ padding: '50px', textAlign: 'center' }}>加载中...</div>
        </Spin>
      </div>
    );
  }

  // 显示重定向中状态
  if (redirecting) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
        <Spin spinning={true} size="large">
          <div style={{ padding: '50px', textAlign: 'center' }}>登录成功，正在跳转...</div>
        </Spin>
      </div>
    );
  }

  return (
    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh', background: '#f0f2f5' }}>
      {contextHolder}
      <Card title="用户登录" style={{ width: 400 }}>
        <Form
          name="login"
          onFinish={onFinish}
          autoComplete="off"
          size="large"
        >
          <Form.Item
            name="userAccount"
            rules={[
              { required: true, message: '请输入用户账号!' },
              { min: 4, message: '账号长度不能小于4位' }
            ]}
          >
            <Input prefix={<UserOutlined />} placeholder="用户账号" />
          </Form.Item>

          <Form.Item
            name="userPassword"
            rules={[
              { required: true, message: '请输入密码!' },
              { min: 8, message: '密码长度不能小于8位' }
            ]}
          >
            <Input.Password
              prefix={<LockOutlined />}
              type="password"
              placeholder="密码"
            />
          </Form.Item>

          <Form.Item>
            <Button type="primary" htmlType="submit" loading={loading} block>
              登录
            </Button>
          </Form.Item>
          
          <Divider />
          
          <div>
            没有账号？ <Link href="/auth/register" style={{ color: '#1890ff' }}>立即注册</Link>
          </div>
        </Form>
      </Card>
    </div>
  );
} 
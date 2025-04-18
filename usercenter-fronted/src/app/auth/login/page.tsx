'use client';

import React, { useState, useEffect } from 'react';
import { Button, Form, Input, message, Card, Divider, Spin } from 'antd';
import { UserOutlined, LockOutlined } from '@ant-design/icons';
import { login, UserLoginParams } from '@/services/userService';
import { useRouter } from 'next/navigation';
import { useUser } from '@/contexts/UserContext';
import styles from '../auth.module.css';

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

  const onFinish = async (values: UserLoginParams) => {
    try {
      setLoading(true);
      const response = await login(values);
      
      messageApi.success('登录成功！');
      
      // 刷新用户信息
      await refreshUserInfo();
      forceUpdate();
      
      // 设置重定向状态
      setRedirecting(true);
      
      // 延迟导航到主页，以便用户看到登录成功消息
      setTimeout(() => {
        router.push('/');
      }, 1000);
    } catch (error: any) {
      messageApi.error(error.message || '登录失败，请检查账号密码');
    } finally {
      setLoading(false);
    }
  };

  // 处理跳转到注册页
  const handleGoToRegister = () => {
    router.push('/auth/register');
  };

  // 添加html/body类，支持滚动
  useEffect(() => {
    document.documentElement.classList.add('auth-page');
    document.body.classList.add('auth-page');
    
    return () => {
      document.documentElement.classList.remove('auth-page');
      document.body.classList.remove('auth-page');
    };
  }, []);

  return (
    <div className={styles.scrollableContainer}>
      {contextHolder}
      <Card 
        className={styles.authCard}
        title={<div className={styles.authTitle}>登录用户中心</div>}
      >
        {redirecting ? (
          <div className={styles.redirectContainer}>
            <Spin spinning size="large" />
            <div className={styles.redirectText}>登录成功，正在跳转...</div>
          </div>
        ) : (
          <Form
            name="login_form"
            initialValues={{ remember: true }}
            onFinish={onFinish}
            size="large"
            layout="vertical"
          >
            <Form.Item
              name="userAccount"
              rules={[{ required: true, message: '请输入用户账号!' }]}
            >
              <Input 
                prefix={<UserOutlined className={styles.inputIcon} />} 
                placeholder="用户账号" 
              />
            </Form.Item>

            <Form.Item
              name="userPassword"
              rules={[{ required: true, message: '请输入密码!' }]}
            >
              <Input.Password
                prefix={<LockOutlined className={styles.inputIcon} />}
                placeholder="密码"
              />
            </Form.Item>

            <Form.Item>
              <Button 
                type="primary" 
                htmlType="submit" 
                className={styles.actionButton} 
                loading={loading}
              >
                登录
              </Button>
            </Form.Item>

            <Divider plain>
              <span className={styles.dividerText}>其他选项</span>
            </Divider>
            
            <div className={styles.linkText}>
              <span>还没有账号？</span>
              <Button type="link" onClick={handleGoToRegister} style={{ padding: 0 }}>
                立即注册
              </Button>
            </div>
          </Form>
        )}
      </Card>
    </div>
  );
} 
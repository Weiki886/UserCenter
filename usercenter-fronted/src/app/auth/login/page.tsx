'use client';

import React, { useState, useEffect } from 'react';
import { Button, Form, Input, message, Card, Divider } from 'antd';
import { UserOutlined, LockOutlined } from '@ant-design/icons';
import { login, UserLoginParams } from '@/services/userService';
import { useRouter } from 'next/navigation';
import { useUser } from '@/contexts/UserContext';
import styles from '../auth.module.css';

export default function LoginPage() {
  const [loading, setLoading] = useState(false);
  const router = useRouter();
  const { refreshUserInfo, currentUser } = useUser();
  const [messageApi, contextHolder] = message.useMessage();

  // 当用户已登录时自动重定向到首页
  useEffect(() => {
    if (currentUser) {
      router.push('/');
    }
  }, [currentUser, router]);

  // 为浏览器后退按钮添加处理
  useEffect(() => {
    // 保存页面来源信息，用于检测后退按钮导航
    if (typeof window !== 'undefined') {
      const previousPage = document.referrer;
      sessionStorage.setItem('loginPreviousPage', previousPage);
    }
  }, []);

  const onFinish = async (values: UserLoginParams) => {
    try {
      setLoading(true);
      // 登录请求
      await login(values);
      
      // 成功消息
      messageApi.success('登录成功！');
      
      // 设置标志，以便刷新用户信息
      localStorage.setItem('loginSuccess', 'true');
      localStorage.setItem('userLoginTime', Date.now().toString());
      
      // 保存登录成功状态，用于后退导航处理
      sessionStorage.setItem('loginSuccessful', 'true');
      
      // 刷新用户信息
      await refreshUserInfo(true);
      
      // 使用正常的router.push而不是replace，这样后退按钮可以正常工作
      router.push('/');
      
      // 对于首次登录后的后退操作，添加特殊处理
      window.addEventListener('popstate', function handlePopState() {
        // 检查是否是返回登录页
        if (window.location.pathname.includes('/auth/login')) {
          // 如果已登录，应该跳回首页
          if (localStorage.getItem('userToken')) {
            // 防止循环导航
            window.removeEventListener('popstate', handlePopState);
            
            // 延迟一点点避免导航冲突
            setTimeout(() => {
              window.location.href = '/';
            }, 100);
          }
        }
      }, { once: true });
      
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
        title={<div className={styles.authTitle}>用户登录</div>}
      >
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
      </Card>
    </div>
  );
} 
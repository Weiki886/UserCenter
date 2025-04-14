'use client';

import React, { useState } from 'react';
import { Button, Form, Input, message, Card, Divider, Spin } from 'antd';
import { UserOutlined, LockOutlined, SafetyOutlined } from '@ant-design/icons';
import { register } from '@/services/userService';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { useUser } from '@/contexts/UserContext';

export default function RegisterPage() {
  const [loading, setLoading] = useState(false);
  const [redirecting, setRedirecting] = useState(false);
  const router = useRouter();
  const { refreshUserInfo, currentUser } = useUser();
  // 创建Message实例以避免被销毁
  const [messageApi, contextHolder] = message.useMessage();

  // 当用户已登录时自动重定向到首页
  React.useEffect(() => {
    if (currentUser) {
      router.push('/');
    }
  }, [currentUser, router]);

  const onFinish = async (values: { userAccount: string; userPassword: string; checkPassword: string }) => {
    try {
      setLoading(true);
      const user = await register(values);
      
      // 标记为重定向状态
      setRedirecting(true);
      
      // 立即保存用户数据到localStorage
      if (typeof window !== 'undefined') {
        localStorage.setItem('userInfo', JSON.stringify(user));
      }
      
      // 刷新用户信息
      await refreshUserInfo();
      
      // 显示成功消息
      messageApi.success(`注册成功！欢迎您，${user.username || user.userAccount}`);
      
      // 使用更可靠的重定向机制
      router.replace('/');
    } catch (error: any) {
      console.error('注册失败:', error);
      // 显示错误消息
      messageApi.error(error.message || '注册失败，请稍后再试');
      setRedirecting(false);
    } finally {
      setLoading(false);
    }
  };

  // 显示重定向中状态
  if (redirecting) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
        <Spin spinning={true} size="large">
          <div style={{ padding: '50px', textAlign: 'center' }}>注册成功，正在跳转...</div>
        </Spin>
      </div>
    );
  }

  return (
    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh', background: '#f0f2f5' }}>
      {contextHolder}
      <Card title="用户注册" style={{ width: 400 }}>
        <Form
          name="register"
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
              placeholder="密码"
            />
          </Form.Item>

          <Form.Item
            name="checkPassword"
            dependencies={['userPassword']}
            rules={[
              { required: true, message: '请确认密码!' },
              ({ getFieldValue }) => ({
                validator(_, value) {
                  if (!value || getFieldValue('userPassword') === value) {
                    return Promise.resolve();
                  }
                  return Promise.reject(new Error('两次输入的密码不一致!'));
                },
              }),
            ]}
          >
            <Input.Password
              prefix={<SafetyOutlined />}
              placeholder="确认密码"
            />
          </Form.Item>

          <Form.Item>
            <Button type="primary" htmlType="submit" loading={loading} block>
              注册
            </Button>
          </Form.Item>
          
          <Divider />
          
          <div>
            已有账号？ <Link href="/auth/login" style={{ color: '#1890ff' }}>立即登录</Link>
          </div>
        </Form>
      </Card>
    </div>
  );
} 
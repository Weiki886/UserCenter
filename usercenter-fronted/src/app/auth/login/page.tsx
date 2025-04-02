'use client';

import React, { useState } from 'react';
import { Button, Form, Input, message, Card, Divider } from 'antd';
import { UserOutlined, LockOutlined } from '@ant-design/icons';
import { login } from '@/services/userService';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { useUser } from '@/contexts/UserContext';

export default function LoginPage() {
  const [loading, setLoading] = useState(false);
  const router = useRouter();
  const { refreshUserInfo } = useUser();

  const onFinish = async (values: { userAccount: string; userPassword: string }) => {
    try {
      setLoading(true);
      await login(values);
      await refreshUserInfo();
      message.success('登录成功！');
      router.push('/');
    } catch (error: any) {
      message.error(error.message || '登录失败，请重试');
    } finally {
      setLoading(false);
    }
  };

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
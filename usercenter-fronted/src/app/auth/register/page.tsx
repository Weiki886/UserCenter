'use client';

import React from 'react';
import { Form, Input, Button, Card, message } from 'antd';
import { useRouter } from 'next/navigation';
import request from '@/utils/request';
import styles from './register.module.css';

const RegisterPage = () => {
  const router = useRouter();

  const onFinish = async (values: any) => {
    try {
      await request.post('/user/register', {
        userAccount: values.username,
        userPassword: values.password,
        checkPassword: values.confirmPassword,
      });
      
      message.success('注册成功！');
      router.push('/auth/login');
    } catch (error: any) {
      message.error(error.response?.data?.message || '注册失败，请重试');
    }
  };

  return (
    <div className={styles.container}>
      <Card title="用户注册" className={styles.card}>
        <Form
          name="register"
          onFinish={onFinish}
          autoComplete="off"
          layout="vertical"
        >
          <Form.Item
            label="用户名"
            name="username"
            rules={[
              { required: true, message: '请输入用户名' },
              { min: 4, message: '用户名至少4个字符' }
            ]}
          >
            <Input />
          </Form.Item>

          <Form.Item
            label="密码"
            name="password"
            rules={[
              { required: true, message: '请输入密码' },
              { min: 8, message: '密码至少8个字符' }
            ]}
          >
            <Input.Password />
          </Form.Item>

          <Form.Item
            label="确认密码"
            name="confirmPassword"
            dependencies={['password']}
            rules={[
              { required: true, message: '请确认密码' },
              ({ getFieldValue }) => ({
                validator(_, value) {
                  if (!value || getFieldValue('password') === value) {
                    return Promise.resolve();
                  }
                  return Promise.reject(new Error('两次输入的密码不一致'));
                },
              }),
            ]}
          >
            <Input.Password />
          </Form.Item>

          <Form.Item>
            <Button type="primary" htmlType="submit" block>
              注册
            </Button>
          </Form.Item>

          <div className={styles.loginLink}>
            已有账号？
            <a onClick={() => router.push('/auth/login')}>立即登录</a>
          </div>
        </Form>
      </Card>
    </div>
  );
};

export default RegisterPage; 
'use client';

import React, { useState, useEffect } from 'react';
import { Form, Input, Button, Card, message, Divider } from 'antd';
import { UserOutlined, LockOutlined } from '@ant-design/icons';
import { useRouter } from 'next/navigation';
import { register, UserRegisterParams } from '@/services/userService';
import styles from '../auth.module.css';

const RegisterPage = () => {
  const router = useRouter();
  const [loading, setLoading] = useState(false);
  const [messageApi, contextHolder] = message.useMessage();

  const onFinish = async (values: any) => {
    try {
      setLoading(true);
      
      const params: UserRegisterParams = {
        userAccount: values.userAccount,
        userPassword: values.userPassword,
        checkPassword: values.checkPassword,
      };
      
      await register(params);
      
      messageApi.success('注册成功！');
      
      // 直接跳转到登录页
      router.push('/auth/login');
    } catch (error: any) {
      messageApi.error(error.message || '注册失败，请重试');
    } finally {
      setLoading(false);
    }
  };

  // 处理跳转到登录页
  const handleGoToLogin = () => {
    router.push('/auth/login');
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
        title={<div className={styles.authTitle}>用户注册</div>}
      >
        <Form
          name="register_form"
          onFinish={onFinish}
          autoComplete="off"
          layout="vertical"
          size="large"
        >
          <Form.Item
            name="userAccount"
            rules={[
              { required: true, message: '请输入用户账号' },
              { min: 4, message: '用户账号至少4个字符' }
            ]}
          >
            <Input 
              prefix={<UserOutlined className={styles.inputIcon} />} 
              placeholder="用户账号" 
            />
          </Form.Item>

          <Form.Item
            name="userPassword"
            rules={[
              { required: true, message: '请输入密码' },
              { min: 8, message: '密码至少8个字符' }
            ]}
          >
            <Input.Password 
              prefix={<LockOutlined className={styles.inputIcon} />} 
              placeholder="密码" 
            />
          </Form.Item>

          <Form.Item
            name="checkPassword"
            rules={[
              { required: true, message: '请确认密码' },
              ({ getFieldValue }) => ({
                validator(_, value) {
                  if (!value || getFieldValue('userPassword') === value) {
                    return Promise.resolve();
                  }
                  return Promise.reject(new Error('两次输入的密码不一致'));
                },
              }),
            ]}
          >
            <Input.Password 
              prefix={<LockOutlined className={styles.inputIcon} />} 
              placeholder="确认密码" 
            />
          </Form.Item>

          <Form.Item>
            <Button 
              type="primary" 
              htmlType="submit" 
              className={styles.actionButton}
              loading={loading}
            >
              注册
            </Button>
          </Form.Item>
          
          <Divider plain>
            <span className={styles.dividerText}>其他选项</span>
          </Divider>
          
          <div className={styles.linkText}>
            <span>已有账号？</span>
            <Button type="link" onClick={handleGoToLogin} style={{ padding: 0 }}>
              立即登录
            </Button>
          </div>
        </Form>
      </Card>
    </div>
  );
};

export default RegisterPage; 
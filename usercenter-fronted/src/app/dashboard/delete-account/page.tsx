'use client';

import { useState } from 'react';
import { Form, Input, Button, Card, message, Alert, Modal } from 'antd';
import { UserOutlined, LockOutlined, ExclamationCircleOutlined } from '@ant-design/icons';
import { useRouter } from 'next/navigation';
import { deleteAccount, UserDeleteParams } from '@/services/userService';
import { useUser } from '@/contexts/UserContext';

export default function DeleteAccountPage() {
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);
  const router = useRouter();
  const { currentUser, clearUserInfo } = useUser();

  const showConfirmModal = (values: UserDeleteParams) => {
    Modal.confirm({
      title: '确认注销账号',
      icon: <ExclamationCircleOutlined />,
      content: '注销账号后，您的个人信息将被删除且无法恢复。确定要继续吗？',
      okText: '确认注销',
      okType: 'danger',
      cancelText: '取消',
      onOk() {
        handleDeleteAccount(values);
      },
    });
  };

  const handleDeleteAccount = async (values: UserDeleteParams) => {
    try {
      setLoading(true);
      await deleteAccount(values);
      
      // 清除本地用户信息
      clearUserInfo();
      
      // 显示成功消息
      message.success('账号已成功注销');
      
      // 使用直接浏览器跳转确保彻底跳转到首页
      window.location.href = '/';
    } catch (error: any) {
      message.error(error.message || '账号注销失败，请重试');
    } finally {
      setLoading(false);
    }
  };

  const onFinish = (values: UserDeleteParams) => {
    showConfirmModal(values);
  };

  return (
    <Card title="注销账号" style={{ maxWidth: 600, margin: '0 auto' }}>
      <Alert
        message="警告"
        description="注销账号是不可逆操作，您的所有个人信息将被永久删除且无法恢复。"
        type="warning"
        showIcon
        style={{ marginBottom: 20 }}
      />
      
      <Form
        form={form}
        name="delete-account"
        layout="vertical"
        onFinish={onFinish}
        initialValues={{ userAccount: currentUser?.userAccount }}
      >
        <Form.Item
          name="userAccount"
          label="账号"
          rules={[{ required: true, message: '请输入您的账号' }]}
        >
          <Input 
            prefix={<UserOutlined />} 
            placeholder="请输入账号" 
          />
        </Form.Item>

        <Form.Item
          name="userPassword"
          label="密码"
          rules={[
            { required: true, message: '请输入密码' },
            { min: 8, message: '密码长度至少为8位' }
          ]}
        >
          <Input.Password 
            prefix={<LockOutlined />} 
            placeholder="请输入密码" 
          />
        </Form.Item>

        <Form.Item>
          <Button 
            danger
            type="primary" 
            htmlType="submit" 
            loading={loading}
            style={{ width: '100%' }}
          >
            确认注销账号
          </Button>
        </Form.Item>
      </Form>
      
      <div style={{ marginTop: '20px', color: '#999', fontSize: '14px' }}>
        <p>注销账号须知：</p>
        <ul>
          <li>账号注销后，您的个人信息将被永久删除</li>
          <li>如需再次使用本系统服务，请重新注册账号</li>
          <li>请确保已备份需要的个人信息</li>
        </ul>
      </div>
    </Card>
  );
} 
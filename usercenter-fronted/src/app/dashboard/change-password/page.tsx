'use client';

import { useState } from 'react';
import { Form, Input, Button, Card, message } from 'antd';
import { LockOutlined } from '@ant-design/icons';
import { useRouter } from 'next/navigation';
import { updatePassword, PasswordUpdateParams } from '@/services/userService';

export default function ChangePasswordPage() {
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);
  const router = useRouter();

  const onFinish = async (values: PasswordUpdateParams) => {
    // Validar que las contraseñas coincidan
    if (values.newPassword !== values.checkPassword) {
      message.error('两次输入的新密码不一致');
      return;
    }

    try {
      setLoading(true);
      await updatePassword(values);
      message.success('密码修改成功！请重新登录');
      
      // Redirigir al usuario a la página de inicio de sesión
      setTimeout(() => {
        router.push('/auth/login');
      }, 1500);
    } catch (error: any) {
      message.error(error.message || '密码修改失败，请重试');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Card title="修改密码" style={{ maxWidth: 600, margin: '0 auto' }}>
      <Form
        form={form}
        name="change-password"
        layout="vertical"
        onFinish={onFinish}
      >
        <Form.Item
          name="oldPassword"
          label="当前密码"
          rules={[
            { required: true, message: '请输入当前密码' },
            { min: 8, message: '密码长度至少为8位' }
          ]}
        >
          <Input.Password 
            prefix={<LockOutlined />} 
            placeholder="请输入当前密码" 
          />
        </Form.Item>

        <Form.Item
          name="newPassword"
          label="新密码"
          rules={[
            { required: true, message: '请输入新密码' },
            { min: 8, message: '密码长度至少为8位' }
          ]}
        >
          <Input.Password 
            prefix={<LockOutlined />} 
            placeholder="请输入新密码" 
          />
        </Form.Item>

        <Form.Item
          name="checkPassword"
          label="确认新密码"
          rules={[
            { required: true, message: '请再次输入新密码' },
            { min: 8, message: '密码长度至少为8位' },
            ({ getFieldValue }) => ({
              validator(_, value) {
                if (!value || getFieldValue('newPassword') === value) {
                  return Promise.resolve();
                }
                return Promise.reject(new Error('两次输入的密码不一致'));
              },
            }),
          ]}
        >
          <Input.Password 
            prefix={<LockOutlined />} 
            placeholder="请再次输入新密码" 
          />
        </Form.Item>

        <Form.Item>
          <Button 
            type="primary" 
            htmlType="submit" 
            loading={loading}
            style={{ width: '100%' }}
          >
            确认修改
          </Button>
        </Form.Item>
      </Form>
      
      <div style={{ marginTop: '20px', color: '#999', fontSize: '14px' }}>
        <p>密码修改提示：</p>
        <ul>
          <li>密码长度至少为8位</li>
          <li>建议使用字母、数字和特殊字符的组合</li>
          <li>修改密码成功后需要重新登录</li>
        </ul>
      </div>
    </Card>
  );
} 
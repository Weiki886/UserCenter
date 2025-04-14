'use client';

import React, { useState, useEffect } from 'react';
import { Form, Input, Button, Card, message, Radio, Spin, Upload } from 'antd';
import { UploadOutlined, UserOutlined, PlusOutlined } from '@ant-design/icons';
import { getCurrentUser, UserType, updateUser } from '@/services/userService';
import type { RcFile, UploadFile, UploadProps } from 'antd/es/upload';
import { useUser } from '@/contexts/UserContext';

// 压缩图片
const compressImage = (file: RcFile): Promise<string> =>
  new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.readAsDataURL(file);
    reader.onload = (e) => {
      const img = new Image();
      img.src = e.target?.result as string;
      img.onload = () => {
        // 创建canvas
        const canvas = document.createElement('canvas');
        // 最大尺寸为 300px
        const MAX_WIDTH = 300;
        const MAX_HEIGHT = 300;
        let width = img.width;
        let height = img.height;
        
        // 调整尺寸
        if (width > height) {
          if (width > MAX_WIDTH) {
            height *= MAX_WIDTH / width;
            width = MAX_WIDTH;
          }
        } else {
          if (height > MAX_HEIGHT) {
            width *= MAX_HEIGHT / height;
            height = MAX_HEIGHT;
          }
        }
        
        canvas.width = width;
        canvas.height = height;
        
        const ctx = canvas.getContext('2d');
        ctx?.drawImage(img, 0, 0, width, height);
        
        // 以低质量jpg格式输出
        const dataUrl = canvas.toDataURL('image/jpeg', 0.6);
        resolve(dataUrl);
      };
      img.onerror = (error) => reject(error);
    };
    reader.onerror = (error) => reject(error);
  });

// 检查文件是否为图片
const beforeUpload = (file: RcFile) => {
  const isImage = file.type.startsWith('image/');
  if (!isImage) {
    message.error('您只能上传图片文件!');
  }
  
  const isLt2M = file.size / 1024 / 1024 < 2;
  if (!isLt2M) {
    message.error('图片大小不能超过2MB!');
  }
  
  return isImage && isLt2M;
};

export default function SettingsPage() {
  const [form] = Form.useForm();
  const [user, setUser] = useState<UserType | null>(null);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [avatarUrl, setAvatarUrl] = useState<string | undefined>(undefined);
  const [fileList, setFileList] = useState<UploadFile[]>([]);
  const [uploading, setUploading] = useState(false);
  const { refreshUserInfo } = useUser();

  useEffect(() => {
    async function fetchUserData() {
      try {
        const userData = await getCurrentUser();
        setUser(userData);
        setAvatarUrl(userData.avatarUrl);
        
        // 如果用户已有头像，将其添加到文件列表
        if (userData.avatarUrl) {
          setFileList([
            {
              uid: '-1',
              name: 'avatar.png',
              status: 'done',
              url: userData.avatarUrl,
            },
          ]);
        }
        
        // 初始化表单
        form.setFieldsValue({
          username: userData.username,
          email: userData.email,
          phone: userData.phone,
          gender: userData.gender,
        });
      } catch (error) {
        message.error('获取用户信息失败');
      } finally {
        setLoading(false);
      }
    }

    fetchUserData();
  }, [form]);

  const onFinish = async (values: any) => {
    try {
      setSubmitting(true);
      
      // 确保values中的avatarUrl是字符串
      const updateValues = {
        ...values,
        avatarUrl // 使用state中的avatarUrl
        // 这里不传ID，因为是修改当前登录用户自己的信息
      };
      
      // 使用updateUser方法
      await updateUser(updateValues);
      message.success('更新成功！');
      
      // 使用UserContext的refreshUserInfo方法更新全局用户信息
      refreshUserInfo();
      
    } catch (error: any) {
      message.error(error.message || '更新失败，请重试');
    } finally {
      setSubmitting(false);
    }
  };

  // 处理文件上传改变
  const handleChange: UploadProps['onChange'] = async (info) => {
    if (info.file.status === 'uploading') {
      setUploading(true);
      return;
    }
    
    if (info.file.status === 'done') {
      // 如果是自定义上传，这里会处理不同的逻辑
      setUploading(false);
    }
    
    // 更新文件列表
    let fileList = [...info.fileList];
    fileList = fileList.slice(-1); // 只保留最后一个文件
    setFileList(fileList);
  };
  
  // 自定义上传
  const customUpload = async (options: any) => {
    const { file, onSuccess, onError } = options;
    
    try {
      // 压缩并转换为Base64
      const base64 = await compressImage(file);
      setAvatarUrl(base64);
      onSuccess("ok");
    } catch (err) {
      onError(err);
    }
  };

  if (loading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '60vh' }}>
        <Spin spinning={true} size="large">
          <div style={{ padding: '50px', textAlign: 'center' }}>加载中...</div>
        </Spin>
      </div>
    );
  }

  return (
    <Card title="个人设置">
      <Form
        form={form}
        layout="vertical"
        onFinish={onFinish}
        style={{ maxWidth: 600 }}
      >
        <Form.Item
          name="username"
          label="昵称"
          rules={[{ message: '请输入昵称' }]}
        >
          <Input placeholder="请输入昵称" />
        </Form.Item>

        <Form.Item
          name="email"
          label="邮箱"
          rules={[
            { type: 'email', message: '请输入有效的邮箱地址' }
          ]}
        >
          <Input placeholder="请输入邮箱" />
        </Form.Item>

        <Form.Item
          name="phone"
          label="手机号"
          rules={[
            { pattern: /^1\d{10}$/, message: '请输入有效的手机号' }
          ]}
        >
          <Input placeholder="请输入手机号" />
        </Form.Item>

        <Form.Item name="gender" label="性别">
          <Radio.Group>
            <Radio value={1}>男</Radio>
            <Radio value={2}>女</Radio>
            <Radio value={0}>不设置</Radio>
          </Radio.Group>
        </Form.Item>

        <Form.Item label="头像">
          <div className="avatar-upload-container">
            <Upload
              name="avatar"
              listType="picture-card"
              className="avatar-uploader"
              showUploadList={false}
              beforeUpload={beforeUpload}
              onChange={handleChange}
              customRequest={customUpload}
              fileList={fileList}
            >
              {avatarUrl ? (
                <img 
                  src={avatarUrl} 
                  alt="avatar" 
                  style={{ width: '100%', height: '100%', objectFit: 'cover' }} 
                />
              ) : (
                <div>
                  {uploading ? <Spin /> : <PlusOutlined />}
                  <div style={{ marginTop: 8 }}>上传</div>
                </div>
              )}
            </Upload>
            <div style={{ marginLeft: 16 }}>
              <p>点击图片框上传您的头像</p>
              <p>支持JPG、PNG格式，文件大小不超过2MB</p>
            </div>
          </div>
        </Form.Item>

        <Form.Item>
          <Button type="primary" htmlType="submit" loading={submitting}>
            保存修改
          </Button>
        </Form.Item>
      </Form>
    </Card>
  );
} 

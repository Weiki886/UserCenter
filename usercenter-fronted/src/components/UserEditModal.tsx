import React, { useState, useEffect } from 'react';
import { Modal, Form, Input, Select, message, Avatar, Upload, Button } from 'antd';
import { UserOutlined, UploadOutlined } from '@ant-design/icons';
import type { UploadFile, UploadProps } from 'antd/es/upload/interface';
import { UserType, updateUser } from '@/services/userService';

interface UserEditModalProps {
  visible: boolean;
  onCancel: () => void;
  onSuccess: () => void;
  user: UserType | null;
}

const { Option } = Select;

const UserEditModal: React.FC<UserEditModalProps> = ({
  visible,
  onCancel,
  onSuccess,
  user,
}) => {
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);
  const [avatarPreview, setAvatarPreview] = useState<string | undefined>(undefined);
  const [fileList, setFileList] = useState<UploadFile[]>([]);

  useEffect(() => {
    if (user && visible) {
      form.setFieldsValue({
        username: user.username,
        avatarUrl: user.avatarUrl,
        gender: user.gender,
        phone: user.phone,
        email: user.email,
      });
      setAvatarPreview(user.avatarUrl);
      
      // Si ya existe un avatar, agregarlo a la lista de archivos
      if (user.avatarUrl) {
        setFileList([
          {
            uid: '-1',
            name: 'avatar.png',
            status: 'done',
            url: user.avatarUrl,
          },
        ]);
      } else {
        setFileList([]);
      }
    }
  }, [user, visible, form]);

  const handleOk = async () => {
    try {
      const values = await form.validateFields();
      setLoading(true);
      
      // Filtrar campos vacíos
      const filteredValues = Object.entries(values).reduce((acc, [key, value]) => {
        // Solo incluir campos con valores no vacíos
        if (value !== "" && value !== undefined && value !== null) {
          acc[key] = value;
        }
        return acc;
      }, {} as Record<string, any>);
      
      console.log('Valores enviados al servidor:', { id: user?.id, ...filteredValues });
      
      await updateUser({
        id: user?.id,
        ...filteredValues,
      });
      
      message.success('更新用户信息成功');
      onSuccess();
    } catch (error: any) {
      const errorMsg = error.message || '更新用户信息失败';
      message.error(errorMsg);
      console.error('更新用户信息失败:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleAvatarChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const url = e.target.value;
    setAvatarPreview(url);
    form.setFieldsValue({ avatarUrl: url });
  };

  // 配置上传组件属性
  const uploadProps: UploadProps = {
    onRemove: () => {
      setFileList([]);
      setAvatarPreview(undefined);
      form.setFieldsValue({ avatarUrl: undefined });
    },
    beforeUpload: (file) => {
      // 使用FileReader读取文件并获取 Data URL
      const reader = new FileReader();
      reader.readAsDataURL(file);
      reader.onload = () => {
        const dataUrl = reader.result as string;
        setAvatarPreview(dataUrl);
        form.setFieldsValue({ avatarUrl: dataUrl });
        setFileList([
          {
            uid: '-1',
            name: file.name,
            status: 'done',
            url: dataUrl,
          },
        ]);
      };
      return false; // 阻止自动上传
    },
    fileList,
    maxCount: 1,
    listType: "picture",
  };

  return (
    <Modal
      title="编辑用户信息"
      open={visible}
      onOk={handleOk}
      onCancel={onCancel}
      confirmLoading={loading}
      destroyOnClose
    >
      <div className="flex flex-col items-center mb-4">
        {avatarPreview ? (
          <>
            <Avatar 
              src={avatarPreview} 
              size={64} 
              style={{ marginBottom: 8 }}
              onError={() => {
                setAvatarPreview(undefined);
                return true;
              }}
            />
          </>
        ) : (
          <Avatar icon={<UserOutlined />} size={64} style={{ marginBottom: 8 }} />
        )}
      </div>
      
      <Form form={form} layout="vertical" preserve={false}>
        <Form.Item
          name="username"
          label="用户名"
        >
          <Input placeholder="请输入用户名" />
        </Form.Item>
        
        <Form.Item
          name="avatarUrl"
          label="用户头像"
        >
          <Input 
            style={{ display: 'none' }}
            onChange={handleAvatarChange}
          />
          <Upload {...uploadProps}>
            <Button icon={<UploadOutlined />}>选择图片</Button>
          </Upload>
        </Form.Item>
        
        <Form.Item name="gender" label="性别">
          <Select placeholder="请选择性别">
            <Option value={0}>女</Option>
            <Option value={1}>男</Option>
          </Select>
        </Form.Item>
        
        <Form.Item
          name="phone"
          label="电话"
          rules={[
            { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确' }
          ]}
        >
          <Input placeholder="请输入电话号码" />
        </Form.Item>
        
        <Form.Item
          name="email"
          label="邮箱"
          rules={[
            { type: 'email', message: '邮箱格式不正确' }
          ]}
        >
          <Input placeholder="请输入邮箱" />
        </Form.Item>
      </Form>
    </Modal>
  );
};

export default UserEditModal; 
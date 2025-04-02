'use client';

import React, { useState, useEffect } from 'react';
import { Card, Descriptions, Avatar, message, Spin } from 'antd';
import { UserType, getCurrentUser } from '@/services/userService';
import { UserOutlined } from '@ant-design/icons';

export default function DashboardPage() {
  const [user, setUser] = useState<UserType | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function fetchUserData() {
      try {
        const userData = await getCurrentUser();
        setUser(userData);
      } catch (error) {
        message.error('获取用户信息失败');
      } finally {
        setLoading(false);
      }
    }

    fetchUserData();
  }, []);

  // 格式化角色
  const formatRole = (role?: number) => {
    return role === 1 ? '管理员' : '普通用户';
  };

  // 格式化状态
  const formatStatus = (status?: number) => {
    return status === 0 ? '正常' : '已封禁';
  };

  // 格式化性别
  const formatGender = (gender?: number) => {
    if (gender === 1) return '男';
    if (gender === 2) return '女';
    return '未设置';
  };

  if (loading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '80vh' }}>
        <Spin size="large" tip="加载中..." />
      </div>
    );
  }

  return (
    <div>
      <Card title="个人信息" style={{ marginBottom: 24 }}>
        <div style={{ display: 'flex', alignItems: 'center', marginBottom: 24 }}>
          <Avatar 
            size={64} 
            icon={<UserOutlined />} 
            src={user?.avatarUrl} 
            style={{ marginRight: 24 }}
          />
          <div>
            <h2 style={{ marginBottom: 4 }}>{user?.username || user?.userAccount || '未设置昵称'}</h2>
            <div>{formatRole(user?.userRole)}</div>
          </div>
        </div>

        <Descriptions bordered column={2}>
          <Descriptions.Item label="账号">{user?.userAccount}</Descriptions.Item>
          <Descriptions.Item label="昵称">{user?.username || '未设置'}</Descriptions.Item>
          <Descriptions.Item label="电子邮箱">{user?.email || '未设置'}</Descriptions.Item>
          <Descriptions.Item label="手机号码">{user?.phone || '未设置'}</Descriptions.Item>
          <Descriptions.Item label="性别">{formatGender(user?.gender)}</Descriptions.Item>
          <Descriptions.Item label="账号状态">{formatStatus(user?.userStatus)}</Descriptions.Item>
          <Descriptions.Item label="注册时间">
            {user?.createTime ? new Date(user.createTime).toLocaleString() : '未知'}
          </Descriptions.Item>
        </Descriptions>
      </Card>
    </div>
  );
} 
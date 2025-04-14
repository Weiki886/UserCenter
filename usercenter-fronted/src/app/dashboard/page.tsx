'use client';

import React, { useState, useEffect, useMemo, useCallback } from 'react';
import { Card, Descriptions, Avatar, message, Spin } from 'antd';
import { UserType, getCurrentUser } from '@/services/userService';
import { UserOutlined } from '@ant-design/icons';

// 使用memo优化子组件
const UserInfoCard = React.memo(({ user }: { user: UserType }) => {
  // 格式化角色
  const formatRole = useCallback((role?: number) => {
    return role === 1 ? '管理员' : '普通用户';
  }, []);

  // 格式化状态
  const formatStatus = useCallback((status?: number) => {
    return status === 0 ? '正常' : '已封禁';
  }, []);

  // 格式化性别
  const formatGender = useCallback((gender?: number) => {
    if (gender === 1) return '男';
    if (gender === 2) return '女';
    return '未设置';
  }, []);

  return (
    <Card 
      title="用户信息" 
      style={{ width: '100%' }}
      extra={
        <Avatar 
          size={64} 
          icon={<UserOutlined />} 
          src={user.avatarUrl} 
        />
      }
    >
      <Descriptions column={2}>
        <Descriptions.Item label="用户名">{user.username}</Descriptions.Item>
        <Descriptions.Item label="账号">{user.userAccount}</Descriptions.Item>
        <Descriptions.Item label="性别">{formatGender(user.gender)}</Descriptions.Item>
        <Descriptions.Item label="手机号">{user.phone || '未设置'}</Descriptions.Item>
        <Descriptions.Item label="邮箱">{user.email || '未设置'}</Descriptions.Item>
        <Descriptions.Item label="角色">{formatRole(user.userRole)}</Descriptions.Item>
        <Descriptions.Item label="状态">{formatStatus(user.userStatus)}</Descriptions.Item>
        <Descriptions.Item label="注册时间">
          {new Date(user.createTime).toLocaleString()}
        </Descriptions.Item>
      </Descriptions>
    </Card>
  );
});

// 设置组件名称，用于调试
UserInfoCard.displayName = 'UserInfoCard';

export default function DashboardPage() {
  const [user, setUser] = useState<UserType | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let isMounted = true;
    
    async function fetchUserData() {
      try {
        const userData = await getCurrentUser();
        // 只有在组件仍然挂载时才更新状态
        if (isMounted) {
          setUser(userData);
          setLoading(false);
        }
      } catch (error) {
        if (isMounted) {
          message.error('获取用户信息失败');
          setLoading(false);
        }
      }
    }

    fetchUserData();
    
    // 清理函数防止内存泄漏
    return () => {
      isMounted = false;
    };
  }, []);

  // 使用useMemo缓存渲染内容
  const renderContent = useMemo(() => {
    if (loading) {
      return (
        <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '70vh' }}>
          <Spin spinning={true} size="large">
            <div style={{ padding: '50px', textAlign: 'center' }}>加载中...</div>
          </Spin>
        </div>
      );
    }
    
    if (!user) {
      return <div>未找到用户信息</div>;
    }
    
    return <UserInfoCard user={user} />;
  }, [loading, user]);

  return renderContent;
} 
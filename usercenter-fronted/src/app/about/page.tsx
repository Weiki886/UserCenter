'use client';

import { Layout, Typography, Card } from 'antd';
import { useState, useEffect } from 'react';
import { getCurrentUser, UserType } from '@/services/userService';
import NavBar from '@/components/NavBar';

const { Content } = Layout;
const { Title, Paragraph } = Typography;

export default function About() {
  const [currentUser, setCurrentUser] = useState<UserType | null>(null);

  useEffect(() => {
    const fetchCurrentUser = async () => {
      try {
        const user = await getCurrentUser();
        if (user) {
          setCurrentUser(user as UserType);
        }
      } catch (error) {
        console.log('未登录状态');
      }
    };

    fetchCurrentUser();
  }, []);

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <NavBar activeItem="about" />

      <Content style={{ padding: '50px 50px', maxWidth: '1200px', margin: '0 auto' }}>
        <div style={{ textAlign: 'center', marginBottom: '60px' }}>
          <Title level={1}>关于系统</Title>
          <Paragraph style={{ fontSize: '18px', maxWidth: '800px', margin: '20px auto' }}>
            用户中心系统是一个现代化的用户管理平台，旨在提供安全、高效、易用的用户账号管理服务。
          </Paragraph>
        </div>

        <div style={{ maxWidth: '800px', margin: '0 auto' }}>
          <Card style={{ marginBottom: '30px', boxShadow: '0 4px 12px rgba(0,0,0,0.05)' }}>
            <Title level={3}>系统架构</Title>
            <Paragraph style={{ fontSize: '16px' }}>
              系统采用前后端分离架构，前端使用React和Ant Design构建友好的用户界面，后端采用Java Spring Boot提供稳定可靠的API服务。
              数据存储使用MySQL，确保数据的持久性和一致性。
              系统支持集群部署，可根据业务需求灵活扩展，满足不同规模的用户管理需求。
            </Paragraph>
          </Card>
          
          <Card style={{ marginBottom: '30px', boxShadow: '0 4px 12px rgba(0,0,0,0.05)' }}>
            <Title level={3}>核心功能</Title>
            <Paragraph style={{ fontSize: '16px' }}>
              <ul>
                <li>用户注册与登录：提供安全的身份验证</li>
                <li>个人信息管理：允许用户维护和更新个人资料</li>
                <li>用户管理：管理员可以查看、搜索和管理用户账号</li>
                <li>安全保障：采用密码加密、权限控制等安全措施</li>
              </ul>
            </Paragraph>
          </Card>
          
          <Card style={{ boxShadow: '0 4px 12px rgba(0,0,0,0.05)' }}>
            <Title level={3}>技术特点</Title>
            <Paragraph style={{ fontSize: '16px' }}>
              <ul>
                <li>响应式设计：适配各种屏幕尺寸，提供一致的用户体验</li>
                <li>模块化开发：系统组件化，便于维护和扩展</li>
                <li>RESTful API：标准化的API设计，便于集成和对接</li>
                <li>多层次安全：采用多重安全机制，保护用户数据安全</li>
                <li>分布式支持：支持分布式部署和水平扩展，满足高并发场景</li>
              </ul>
            </Paragraph>
          </Card>
        </div>
      </Content>
    </Layout>
  );
} 
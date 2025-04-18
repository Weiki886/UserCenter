'use client';

import { useRouter } from 'next/navigation';
import { Button, Layout, Typography, Row, Col, Card, Space, Avatar } from 'antd';
import { SettingOutlined, TeamOutlined, SecurityScanOutlined } from '@ant-design/icons';
import NavBar from '@/components/NavBar';
import { useUser } from '@/contexts/UserContext';
import { useState, useEffect, useRef } from 'react';

const { Content } = Layout;
const { Title, Paragraph } = Typography;

export default function Home() {
  const router = useRouter();
  const { currentUser, loading, refreshUserInfo } = useUser();
  const [isClient, setIsClient] = useState(false);
  const contentRef = useRef<HTMLDivElement>(null);
  const [forceRender, setForceRender] = useState(0);
  
  // 检测页面可见性并处理导航事件
  useEffect(() => {
    setIsClient(true);
    
    // 检查是否从登录页面返回
    const checkBackNavigation = () => {
      // 尝试从session中获取登录页信息
      const wasOnLoginPage = sessionStorage.getItem('loginPreviousPage')?.includes('/auth/login');
      const isFromLogin = document.referrer.includes('/auth/login');
      
      if (wasOnLoginPage || isFromLogin) {
        console.log('从登录页面返回...');
        
        // 确保内容显示
        if (contentRef.current) {
          contentRef.current.style.display = 'block';
          contentRef.current.style.visibility = 'visible';
          contentRef.current.style.opacity = '1';
        }
        
        // 强制重新渲染
        setForceRender(prev => prev + 1);
        
        // 如果有登录成功标记，尝试刷新用户信息
        const loginSuccess = localStorage.getItem('loginSuccess');
        if (loginSuccess === 'true') {
          refreshUserInfo(true).then(() => {
            localStorage.removeItem('loginSuccess');
          }).catch(err => {
            console.error('刷新用户信息失败:', err);
            localStorage.removeItem('loginSuccess');
          });
        }
      }
    };
    
    // 初始检查一次
    checkBackNavigation();
    
    // 监听历史状态变化
    const handlePopState = () => {
      console.log('检测到后退按钮...');
      checkBackNavigation();
    };
    
    window.addEventListener('popstate', handlePopState);
    
    return () => {
      window.removeEventListener('popstate', handlePopState);
    };
  }, [refreshUserInfo]);

  // 登录状态检测 - 简化逻辑，避免不必要的复杂条件
  const isLoggedIn = isClient && (
    currentUser !== null || 
    localStorage.getItem('userInfo') !== null ||
    localStorage.getItem('userToken') !== null
  );

  // 始终渲染内容，使用key强制更新组件
  return (
    <Layout style={{ 
      minHeight: '100vh', 
      width: '100%', 
      margin: 0, 
      padding: 0,
      overflow: 'hidden',
      maxWidth: '100vw'
    }}>
      <NavBar activeItem="home" />

      <Content 
        ref={contentRef}
        key={`content-${forceRender}`}
        className="home-content"
        style={{ 
          padding: '0 50px', 
          paddingTop: '30px', 
          width: '100%',
          overflow: 'hidden',
          maxWidth: '100vw'
        }}
      >
        <div style={{ textAlign: 'center', marginBottom: '60px' }}>
          <Title level={1}>用户中心系统</Title>
          <Paragraph style={{ fontSize: '18px', margin: '0 auto' }}>
            一站式用户管理平台，提供完善的用户注册、认证、授权和管理功能
          </Paragraph>
          {!isLoggedIn && (
            <Space style={{ marginTop: '20px' }}>
              <Button type="primary" size="large" onClick={() => router.push('/auth/login')}>
                立即登录
              </Button>
              <Button size="large" onClick={() => router.push('/auth/register')}>
                注册账号
              </Button>
            </Space>
          )}
        </div>

        <Row gutter={[24, 24]}>
          <Col xs={24} sm={12} md={8}>
            <Card hoverable style={{ height: '100%' }}>
              <SettingOutlined style={{ fontSize: '36px', color: '#1890ff', marginBottom: '16px' }} />
              <Title level={4}>个人设置</Title>
              <Paragraph>
                灵活的个人资料管理，包括头像设置、基本信息更新、密码修改等功能，让用户掌控自己的个人信息。
              </Paragraph>
            </Card>
          </Col>
          <Col xs={24} sm={12} md={8}>
            <Card hoverable style={{ height: '100%' }}>
              <TeamOutlined style={{ fontSize: '36px', color: '#1890ff', marginBottom: '16px' }} />
              <Title level={4}>用户管理</Title>
              <Paragraph>
                管理员可以进行用户管理，包括查看用户列表、搜索用户、禁用账号等操作，高效管理平台用户。
              </Paragraph>
            </Card>
          </Col>
          <Col xs={24} sm={12} md={8}>
            <Card hoverable style={{ height: '100%' }}>
              <SecurityScanOutlined style={{ fontSize: '36px', color: '#1890ff', marginBottom: '16px' }} />
              <Title level={4}>安全保障</Title>
              <Paragraph>
                采用业界标准的安全措施保护用户数据，包括密码加密存储、敏感信息保护、登录安全控制等。
              </Paragraph>
            </Card>
          </Col>
        </Row>
      </Content>
    </Layout>
  );
}

'use client';

import { useRouter } from 'next/navigation';
import { Button, Layout, Typography, Row, Col, Card, Space, Avatar, Spin } from 'antd';
import { SettingOutlined, TeamOutlined, SecurityScanOutlined } from '@ant-design/icons';
import NavBar from '@/components/NavBar';
import { useUser } from '@/contexts/UserContext';
import { useState, useEffect } from 'react';

const { Content, Footer } = Layout;
const { Title, Paragraph } = Typography;

export default function Home() {
  const router = useRouter();
  const { currentUser, loading } = useUser();
  const [isClient, setIsClient] = useState(false);
  
  // 客户端检测
  useEffect(() => {
    setIsClient(true);
  }, []);

  // 检查是否真正登录，结合Context和localStorage
  const isLoggedIn = isClient
    ? currentUser !== null || localStorage.getItem('userInfo') !== null
    : currentUser !== null;

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <NavBar activeItem="home" />

      <Content style={{ padding: '50px 50px', maxWidth: '1200px', margin: '0 auto' }}>
        {loading ? (
          <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '50vh' }}>
            <Spin spinning={true} size="large">
              <div style={{ padding: '50px', textAlign: 'center' }}>加载中...</div>
            </Spin>
          </div>
        ) : (
          <>
            <div style={{ textAlign: 'center', marginBottom: '60px' }}>
              <Title level={1}>用户中心系统</Title>
              <Paragraph style={{ fontSize: '18px', maxWidth: '800px', margin: '0 auto' }}>
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
          </>
        )}
      </Content>

      <Footer style={{ 
        textAlign: 'center',
        background: '#fff',
        color: '#000',
        padding: '12px 0',
        borderTop: '1px solid #f0f0f0'
      }}>
        用户中心系统 ©{new Date().getFullYear()} by 凭君语未可
      </Footer>
    </Layout>
  );
}

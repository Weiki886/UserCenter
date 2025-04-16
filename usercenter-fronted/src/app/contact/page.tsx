'use client';

import { Layout, Typography, Card } from 'antd';
import { useState, useEffect } from 'react';
import { getCurrentUser, UserType } from '@/services/userService';
import NavBar from '@/components/NavBar';

const { Content } = Layout;
const { Title, Paragraph } = Typography;

export default function Contact() {
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
      <NavBar activeItem="contact" />

      <Content style={{ padding: '50px 50px', maxWidth: '1200px', margin: '0 auto' }}>
        <div style={{ textAlign: 'center', marginBottom: '60px' }}>
          <Title level={1}>联系我们</Title>
          <Paragraph style={{ fontSize: '18px', maxWidth: '800px', margin: '20px auto' }}>
            如有任何问题或建议，欢迎随时与我们联系。我们将尽快回复您的询问。
          </Paragraph>
        </div>

        <div style={{ display: 'flex', justifyContent: 'center' }}>
          <Card style={{ width: 400, textAlign: 'center', boxShadow: '0 4px 12px rgba(0,0,0,0.1)' }}>
            <Title level={3}>联系方式</Title>
            <div style={{ margin: '30px 0' }}>
              <Title level={4}>电子邮箱</Title>
              <Paragraph style={{ fontSize: '16px' }}>
                <a href="mailto:Weiki886@163.com">Weiki886@163.com</a>
              </Paragraph>
            </div>
          </Card>
        </div>
      </Content>
    </Layout>
  );
} 
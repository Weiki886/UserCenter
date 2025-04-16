'use client';

import React from 'react';
import { Layout } from 'antd';
import Link from 'next/link';

const Footer: React.FC = () => {
  return (
    <Layout.Footer style={{ 
      textAlign: 'center',
      background: '#fff',
      color: '#000',
      padding: '16px 0',
      borderTop: '1px solid #f0f0f0'
    }}>
      <div className="footer-content">
        <div className="footer-links" style={{ marginBottom: '10px' }}>
          <Link href="/" style={{ margin: '0 10px', color: '#1890ff' }}>首页</Link>
          <Link href="/about" style={{ margin: '0 10px', color: '#1890ff' }}>关于我们</Link>
          <Link href="/contact" style={{ margin: '0 10px', color: '#1890ff' }}>联系我们</Link>
        </div>
        <div className="footer-copyright">
          用户中心系统 ©{new Date().getFullYear()} by 凭君语未可
        </div>
      </div>
    </Layout.Footer>
  );
};

export default Footer; 
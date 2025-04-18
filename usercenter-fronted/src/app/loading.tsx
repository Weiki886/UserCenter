import { Spin } from 'antd';

export default function Loading() {
  return (
    <div style={{ 
      height: '100vh', 
      width: '100%', 
      display: 'flex', 
      justifyContent: 'center', 
      alignItems: 'center', 
      flexDirection: 'column',
      gap: '16px',
      background: '#f5f5f5'
    }}>
      <Spin size="large" />
      <div>加载中，请稍候...</div>
    </div>
  );
} 
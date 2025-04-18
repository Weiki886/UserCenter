'use client';

import React, { ReactNode, useEffect, useState } from 'react';

// 添加错误边界组件
class ErrorBoundary extends React.Component<
  { children: ReactNode, fallback: ReactNode },
  { hasError: boolean }
> {
  constructor(props: { children: ReactNode, fallback: ReactNode }) {
    super(props);
    this.state = { hasError: false };
  }

  static getDerivedStateFromError() {
    return { hasError: true };
  }

  componentDidCatch(error: Error, errorInfo: React.ErrorInfo) {
    console.error("页面渲染错误:", error, errorInfo);
  }

  render() {
    if (this.state.hasError) {
      return this.props.fallback;
    }
    return this.props.children;
  }
}

// 备用UI组件，当主内容无法渲染时显示
const FallbackUI = () => (
  <div style={{ 
    width: '100%', 
    height: '100vh', 
    display: 'flex', 
    flexDirection: 'column',
    justifyContent: 'center', 
    alignItems: 'center',
    padding: '20px',
    textAlign: 'center'
  }}>
    <h1>用户中心系统</h1>
    <p>页面加载中，请稍候...</p>
    <p>如长时间未加载，请<a href="/" style={{ color: '#1890ff', cursor: 'pointer' }}>刷新页面</a></p>
  </div>
);

export default function ClientWrapper({ children }: { children: ReactNode }) {
  const [isClient, setIsClient] = useState(false);
  const [renderKey, setRenderKey] = useState(0);
  const [isNavigatingBack, setIsNavigatingBack] = useState(false);

  // 监听路由变化和浏览器后退操作
  useEffect(() => {
    if (typeof window === 'undefined') return;
    
    // 标记为客户端渲染
    setIsClient(true);
    
    // 处理浏览器后退按钮事件
    const handlePopState = () => {
      // 标记正在使用后退按钮
      setIsNavigatingBack(true);
      
      // 强制组件重新渲染
      setRenderKey(prev => prev + 1);
      
      // 如果是从登录页面返回，确保页面正确显示
      const fromLoginPage = document.referrer.includes('/auth/login');
      if (fromLoginPage) {
        // 延迟一点时间，确保DOM已更新
        setTimeout(() => {
          // 清除可能的加载状态
          const loadingElements = document.querySelectorAll('.ant-spin');
          loadingElements.forEach(el => {
            if (el.parentElement) {
              el.parentElement.style.display = 'none';
            }
          });
          
          // 确保内容可见
          const contentElements = document.querySelectorAll('.ant-layout-content');
          contentElements.forEach(el => {
            el.classList.add('force-visible');
          });
        }, 100);
      }
    };

    // 注册浏览器历史事件监听
    window.addEventListener('popstate', handlePopState);
    
    // 初始渲染时也触发一次重新渲染
    const timer = setTimeout(() => {
      setRenderKey(prev => prev + 1);
    }, 50);
    
    // 重置滚动条设置
    document.body.style.overflow = 'auto';
    document.documentElement.style.overflow = 'auto';
    
    // 添加样式
    const style = document.createElement('style');
    style.textContent = `
      html::-webkit-scrollbar, body::-webkit-scrollbar {
        display: none !important;
        width: 0 !important;
        height: 0 !important;
      }
      html, body {
        scrollbar-width: none !important;
        -ms-overflow-style: none !important;
      }
      .force-visible {
        display: block !important;
        visibility: visible !important;
        opacity: 1 !important;
      }
    `;
    document.head.appendChild(style);
    
    return () => {
      window.removeEventListener('popstate', handlePopState);
      document.head.removeChild(style);
      clearTimeout(timer);
    };
  }, []);

  // 当不在客户端时，显示简单的加载内容
  if (!isClient) {
    return <div style={{ height: '100vh', width: '100%', display: 'flex', justifyContent: 'center', alignItems: 'center' }}>
      <p>加载中...</p>
    </div>;
  }

  return (
    <ErrorBoundary fallback={<FallbackUI />}>
      <div key={renderKey} style={{ width: '100%', height: '100%', minHeight: '100vh' }}>
        {children}
      </div>
    </ErrorBoundary>
  );
} 
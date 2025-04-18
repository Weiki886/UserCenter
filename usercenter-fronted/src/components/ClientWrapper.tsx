'use client';

import React, { ReactNode, useEffect, useState } from 'react';

export default function ClientWrapper({ children }: { children: ReactNode }) {
  const [isClient, setIsClient] = useState(false);

  useEffect(() => {
    // 在客户端渲染后标记为客户端模式
    setIsClient(true);
    
    // 重置滚动条设置，确保页面可滚动但隐藏滚动条
    document.body.style.overflow = 'auto';
    document.documentElement.style.overflow = 'auto';
    
    // 添加隐藏滚动条的样式
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
    `;
    document.head.appendChild(style);
    
    return () => {
      document.head.removeChild(style);
    };
  }, []);

  return (
    <>
      {isClient ? children : <div style={{ height: '100vh', overflow: 'auto', scrollbarWidth: 'none', msOverflowStyle: 'none' }} className="hide-scrollbar"></div>}
    </>
  );
} 
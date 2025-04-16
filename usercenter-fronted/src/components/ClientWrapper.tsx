'use client';

import React, { useState, useEffect, ReactNode } from 'react';

interface ClientWrapperProps {
  children: ReactNode;
}

// 客户端包装器组件，解决水合不一致问题
const ClientWrapper: React.FC<ClientWrapperProps> = ({ children }) => {
  const [mounted, setMounted] = useState(false);
  
  useEffect(() => {
    setMounted(true);
  }, []);
  
  return mounted ? <>{children}</> : null;
};

export default ClientWrapper; 
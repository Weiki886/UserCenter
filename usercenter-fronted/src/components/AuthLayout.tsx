'use client';

import React, { ReactNode } from 'react';

interface AuthLayoutProps {
  children: ReactNode;
}

export default function AuthLayoutClient({ children }: AuthLayoutProps) {
  return (
    <div
      style={{
        width: '100%',
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        minHeight: '100vh',
        overflow: 'auto',
        padding: '20px 0',
        scrollbarWidth: 'none',
        msOverflowStyle: 'none'
      }}
      className="hide-scrollbar"
    >
      {children}
    </div>
  );
} 
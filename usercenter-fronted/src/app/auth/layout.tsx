import type { Metadata } from "next";
import { Inter } from "next/font/google";
import "../globals.css";
import { ConfigProvider } from 'antd';
import zhCN from 'antd/locale/zh_CN';
import { UserProvider } from "@/contexts/UserContext";
import AuthLayoutClient from "@/components/AuthLayout";

const inter = Inter({ subsets: ["latin"] });

export const metadata: Metadata = {
  title: "用户中心系统",
  description: "用户中心系统 - 基于React18和Ant Design",
  icons: {
    icon: [
      { url: '/icon/favicon.ico' },
      { url: '/icon/icon.png', type: 'image/png', sizes: '32x32' },
    ],
    apple: [
      { url: '/icon/apple-icon.png', sizes: '180x180', type: 'image/png' },
    ],
  },
};

export default function AuthLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="zh-CN" className="auth-page">
      <body className={`${inter.className} auth-page`} style={{ 
        backgroundColor: '#f5f5f5',
        overflowY: 'auto',
        height: '100%',
        msOverflowStyle: 'none',
        scrollbarWidth: 'none'
      }}>
        <ConfigProvider locale={zhCN}>
          <UserProvider>
            <AuthLayoutClient>
              {children}
            </AuthLayoutClient>
          </UserProvider>
        </ConfigProvider>
      </body>
    </html>
  );
}
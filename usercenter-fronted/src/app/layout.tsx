import type { Metadata } from "next";
import { Inter } from "next/font/google";
import "./globals.css";
import { ConfigProvider } from 'antd';
import zhCN from 'antd/locale/zh_CN';
import { UserProvider } from "@/contexts/UserContext";
import Footer from "@/components/Footer";
import ClientWrapper from "@/components/ClientWrapper";

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

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="zh-CN">
      <head>
        <meta name="viewport" content="width=device-width, initial-scale=1.0" />
      </head>
      <body className={inter.className} style={{ 
        margin: 0, 
        padding: 0, 
        background: '#f5f5f5', 
      }}>
        <ConfigProvider locale={zhCN}>
          <UserProvider>
            <div style={{ 
              display: 'flex', 
              flexDirection: 'column', 
              minHeight: '100vh', 
              width: '100%',
            }}>
              <div style={{ 
                flex: '1 0 auto', 
                width: '100%', 
                position: 'relative',
              }}>
                <ClientWrapper>
                  {children}
                </ClientWrapper>
              </div>
              <Footer />
            </div>
          </UserProvider>
        </ConfigProvider>
      </body>
    </html>
  );
}
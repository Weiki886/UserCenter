'use client'

import { useEffect } from 'react'
import { Button, Result } from 'antd'

export default function Error({
  error,
  reset,
}: {
  error: Error & { digest?: string }
  reset: () => void
}) {
  useEffect(() => {
    // 记录错误到控制台或错误跟踪服务
    console.error('页面发生错误:', error)
  }, [error])

  return (
    <div style={{ 
      height: '100vh', 
      width: '100%', 
      display: 'flex', 
      justifyContent: 'center', 
      alignItems: 'center', 
      padding: '20px' 
    }}>
      <Result
        status="error"
        title="页面加载失败"
        subTitle="抱歉，页面加载过程中出现错误。"
        extra={[
          <Button type="primary" key="refresh" onClick={reset}>
            重试
          </Button>,
          <Button key="home" onClick={() => window.location.href = '/'}>
            返回首页
          </Button>,
        ]}
      />
    </div>
  )
} 
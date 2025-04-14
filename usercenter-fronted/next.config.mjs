/** @type {import('next').NextConfig} */
const nextConfig = {
  async rewrites() {
    return [
      {
        source: '/api/:path*',
        destination: 'http://localhost:8083/:path*' // 转发到后端服务器
      }
    ]
  },
  // 启用图片优化
  images: {
    domains: ['localhost'],
    formats: ['image/avif', 'image/webp'],
  },
  // 启用增量编译
  experimental: {
    // 保留支持的优化配置
    optimizeCss: true,
    optimizePackageImports: ['antd', '@ant-design/icons'],
  },
  // 优化构建
  swcMinify: true,
  poweredByHeader: false,
  // 禁用严格模式以减少重复渲染
  reactStrictMode: false,
  // 减少控制台警告输出
  output: 'standalone',
};

export default nextConfig;

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
  }
};

export default nextConfig;

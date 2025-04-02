/** @type {import('next').NextConfig} */
const nextConfig = {
  async rewrites() {
    return [
      {
        source: '/api/:path*',
        destination: 'http://localhost:8083/:path*' // 转发到后端服务器
      }
    ]
  }
};

export default nextConfig;

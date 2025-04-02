import axios from 'axios';
import { BaseResponse } from './userService';

// 创建axios实例
const api = axios.create({
  baseURL: '/api', // 通过Next.js代理转发到后端
  timeout: 10000,
  withCredentials: true, // 跨域请求时是否需要使用凭证
});

// 请求拦截器
api.interceptors.request.use(
  (config) => {
    // 可以在这里添加认证token等
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 响应拦截器 - 直接返回原始响应，处理逻辑在各个服务函数中
api.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    return Promise.reject(error);
  }
);

export default api; 
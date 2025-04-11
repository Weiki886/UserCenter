import axios, { AxiosInstance, AxiosResponse } from 'axios';
import { BaseResponse } from './userService';

// 创建axios实例
const api = axios.create({
  baseURL: '/api', // 通过Next.js代理转发到后端
  timeout: 10000,
  withCredentials: true, // 跨域请求时是否需要使用凭证
});

// 简单的请求缓存实现
const cache = new Map();
const CACHE_DURATION = 60000; // 缓存有效期1分钟

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

// 响应拦截器
api.interceptors.response.use(
  (response) => {
    return response;
  },
  async (error) => {
    const originalRequest = error.config;
    
    // 重试机制：如果请求失败且没有重试过，最多重试2次
    if (error.response && error.response.status >= 500 && !originalRequest._retry && originalRequest._retryCount < 2) {
      originalRequest._retry = true;
      originalRequest._retryCount = (originalRequest._retryCount || 0) + 1;
      
      // 指数退避策略
      const delay = Math.pow(2, originalRequest._retryCount) * 1000;
      await new Promise(resolve => setTimeout(resolve, delay));
      
      return api(originalRequest);
    }
    
    return Promise.reject(error);
  }
);

// 扩展axios实例，添加缓存功能
interface ApiWithCache extends AxiosInstance {
  getCached<T = any>(url: string, config?: any): Promise<AxiosResponse<T>>;
  clearCache(): void;
}

const apiWithCache = api as ApiWithCache;

// 添加缓存方法
apiWithCache.getCached = async function<T>(url: string, config?: any): Promise<AxiosResponse<T>> {
  const cacheKey = `${url}${JSON.stringify(config?.params || {})}`;
  
  // 检查缓存
  if (cache.has(cacheKey)) {
    const cachedData = cache.get(cacheKey);
    if (Date.now() - cachedData.timestamp < CACHE_DURATION) {
      return cachedData.response;
    } else {
      cache.delete(cacheKey);
    }
  }
  
  // 发送实际请求
  const response = await api.get<T>(url, config);
  
  // 缓存结果
  cache.set(cacheKey, {
    response,
    timestamp: Date.now()
  });
  
  return response;
};

apiWithCache.clearCache = function() {
  cache.clear();
};

export default apiWithCache; 
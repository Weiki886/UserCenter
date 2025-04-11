import axios, { AxiosInstance, AxiosResponse } from 'axios';
import { BaseResponse } from './userService';

// 创建axios实例
const api = axios.create({
  baseURL: '/api', // 通过Next.js代理转发到后端
  timeout: 10000,
  withCredentials: true, // 跨域请求时是否需要使用凭证
});

// 定义API扩展类型
interface ApiWithCache extends AxiosInstance {
  getCached<T>(url: string, config?: any): Promise<AxiosResponse<T>>;
  clearCache(): void;
  invalidateCache(url: string): void;
  invalidateCachePattern(pattern: RegExp): void;
}

// 简单的请求缓存实现
const cache = new Map();
const CACHE_DURATION = 60000; // 缓存有效期1分钟

// 请求拦截器
api.interceptors.request.use(
  (config) => {
    // 只在客户端环境下从localStorage获取token并添加到请求头
    if (typeof window !== 'undefined') {
      const token = localStorage.getItem('userToken');
      if (token) {
        config.headers = config.headers || {};
        config.headers['Authorization'] = `Bearer ${token}`;
      }
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 响应拦截器
api.interceptors.response.use(
  (response) => {
    // 只在客户端执行localStorage操作
    if (typeof window !== 'undefined') {
      // 检查是否是登录响应，如果是则保存token
      const url = response.config.url;
      if (url && url.includes('/user/login') && response.data.code === 0) {
        // 保存token到localStorage (注意：由于后端可能没有在响应中直接返回token，以下是假设的处理方式)
        if (response.headers && response.headers.authorization) {
          localStorage.setItem('userToken', response.headers.authorization);
        } else if (response.data.data && response.data.data.token) {
          localStorage.setItem('userToken', response.data.data.token);
        } else {
          // 没有token但用户已登录，生成临时token
          localStorage.setItem('userToken', `temp_${Date.now()}`);
        }
      }
    }
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

// 清除所有缓存
apiWithCache.clearCache = function() {
  cache.clear();
};

// 清除特定URL的缓存
apiWithCache.invalidateCache = function(url: string) {
  // 使用Array.from来避免直接迭代Map keys
  Array.from(cache.keys()).forEach(key => {
    if (key.startsWith(url)) {
      cache.delete(key);
    }
  });
};

// 根据正则表达式清除缓存
apiWithCache.invalidateCachePattern = function(pattern: RegExp) {
  // 使用Array.from来避免直接迭代Map keys
  Array.from(cache.keys()).forEach(key => {
    if (pattern.test(key)) {
      cache.delete(key);
    }
  });
};

export default apiWithCache; 
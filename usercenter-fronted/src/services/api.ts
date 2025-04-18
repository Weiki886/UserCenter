import axios, { AxiosInstance, AxiosResponse } from 'axios';
import { BaseResponse } from './userService';

// 创建axios实例
const api = axios.create({
  baseURL: '/api', // 通过Next.js代理转发到后端
  timeout: 10000,
  withCredentials: true, // 跨域请求时是否需要使用凭证
});

// 自定义API错误接口
interface ApiError extends Error {
  response?: any;
  status?: number;
  code?: number;
}

// 定义API扩展类型
interface ApiWithCache extends AxiosInstance {
  getCached<T>(url: string, config?: any): Promise<AxiosResponse<T>>;
  clearCache(): void;
  invalidateCache(url: string): void;
  invalidateCachePattern(pattern: RegExp): void;
}

// 添加请求拦截器
api.interceptors.request.use(
  (config) => {
    // 如果有token，添加到请求头
    const token = typeof window !== 'undefined' ? localStorage.getItem('userToken') : null;
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    
    // 防止缓存GET请求
    if (config.method?.toLowerCase() === 'get') {
      config.params = {
        ...config.params,
        _t: Date.now(),
      };
    }
    
    return config;
  },
  (error) => {
    console.error('请求错误:', error);
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
      if (url && url.includes('/user/login') && response.data.code === 0 && response.data.data) {
        let token = null;
        
        // 确保只有在显式登录成功时才设置token
        if (response.headers && response.headers.authorization) {
          token = response.headers.authorization;
          localStorage.setItem('userToken', token);
        } else if (response.data.data && response.data.data.token) {
          token = response.data.data.token;
          localStorage.setItem('userToken', token);
        } else {
          // 没有token但用户已登录，生成临时token
          token = `temp_${Date.now()}`;
          localStorage.setItem('userToken', token);
        }
        
        // 设置登录成功标志，供其他组件检测
        localStorage.setItem('loginSuccess', 'true');
        
        // 设置登录时间戳
        localStorage.setItem('userLoginTime', Date.now().toString());
        
        // 记录日志
        console.log('API拦截器: 检测到登录成功，设置token和标志:', { 
          token, 
          url,
          timestamp: new Date().toISOString()
        });
      }
    }
    return response;
  },
  async (error) => {
    const originalRequest = error.config;
    
    // 如果响应中包含错误信息，将其格式化以便前端显示
    if (error.response && error.response.data) {
      // 处理标准BaseResponse格式的错误
      const errorData = error.response.data;
      // 创建自定义错误对象，包含详细信息
      const enhancedError = new Error(
        errorData.description || errorData.message || error.message || '请求失败'
      ) as ApiError;
      // 保留原始错误信息
      enhancedError.name = 'ApiError';
      enhancedError.response = error.response;
      enhancedError.status = error.response.status;
      enhancedError.code = errorData.code;
      
      // 如果是未登录错误，可以清除本地存储的登录状态
      if (errorData.code === 40100) {
        if (typeof window !== 'undefined') {
          localStorage.removeItem('userToken');
          localStorage.removeItem('userInfo');
          localStorage.removeItem('loginSuccess');
        }
      }
      
      return Promise.reject(enhancedError);
    }
    
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

// 简单的内存缓存
const cache = new Map<string, {
  data: any;
  timestamp: number;
}>();

// 缓存过期时间（5分钟）
const CACHE_EXPIRATION = 5 * 60 * 1000;

// 扩展API类型，添加缓存相关方法
(api as ApiWithCache).getCached = async function<T>(url: string, config = {}) {
  const cacheKey = url + JSON.stringify(config);
  const cachedItem = cache.get(cacheKey);
  
  // 检查是否有有效缓存
  if (cachedItem && Date.now() - cachedItem.timestamp < CACHE_EXPIRATION) {
    return Promise.resolve(cachedItem.data);
  }
  
  // 没有缓存或缓存已过期，发送请求并缓存结果
  try {
    const response = await this.get<T>(url, config);
    cache.set(cacheKey, {
      data: response,
      timestamp: Date.now()
    });
    return response;
  } catch (error) {
    // 错误不缓存，直接抛出
    throw error;
  }
};

// 清除所有缓存
(api as ApiWithCache).clearCache = function() {
  cache.clear();
};

// 清除指定URL的缓存
(api as ApiWithCache).invalidateCache = function(url: string) {
  // 使用Array.from处理Map keys
  Array.from(cache.keys()).forEach(key => {
    if (key.startsWith(url)) {
      cache.delete(key);
    }
  });
};

// 根据正则表达式模式清除缓存
(api as ApiWithCache).invalidateCachePattern = function(pattern: RegExp) {
  // 使用Array.from处理Map keys
  Array.from(cache.keys()).forEach(key => {
    if (pattern.test(key)) {
      cache.delete(key);
    }
  });
};

export default api as ApiWithCache; 
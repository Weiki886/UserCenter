import axios, { AxiosInstance, InternalAxiosRequestConfig, AxiosResponse } from 'axios';
import { message } from 'antd';

// 创建axios实例
const request: AxiosInstance = axios.create({
  baseURL: process.env.REACT_APP_API_BASE_URL || '/api',
  timeout: 10000,
});

// 请求队列，用于存储等待token刷新的请求
let refreshSubscribers: ((token: string) => void)[] = [];
let isRefreshing = false;

// 将请求添加到队列
const subscribeTokenRefresh = (cb: (token: string) => void) => {
  refreshSubscribers.push(cb);
};

// 执行队列中的请求
const onRefreshed = (token: string) => {
  refreshSubscribers.map(cb => cb(token));
  refreshSubscribers = [];
};

// 请求拦截器
request.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = localStorage.getItem('accessToken');
    if (token) {
      config.headers = config.headers || {};
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 响应拦截器
request.interceptors.response.use(
  (response: AxiosResponse) => {
    return response.data;
  },
  async (error) => {
    const originalRequest = error.config;
    
    // 如果是401错误（未授权）且不是刷新token的请求
    if (error.response?.status === 401 && !originalRequest._retry) {
      if (isRefreshing) {
        // 如果正在刷新token，将请求加入队列
        return new Promise(resolve => {
          subscribeTokenRefresh(token => {
            originalRequest.headers.Authorization = `Bearer ${token}`;
            resolve(request(originalRequest));
          });
        });
      }

      originalRequest._retry = true;
      isRefreshing = true;
      
      try {
        // 尝试刷新token
        const refreshToken = localStorage.getItem('refreshToken');
        if (!refreshToken) {
          throw new Error('No refresh token available');
        }

        const response = await axios.post('/api/auth/refresh', {
          refreshToken: refreshToken
        });
        
        const { accessToken, refreshToken: newRefreshToken } = response.data;
        
        // 更新存储的token
        localStorage.setItem('accessToken', accessToken);
        localStorage.setItem('refreshToken', newRefreshToken);
        
        // 更新原始请求的header并重试
        originalRequest.headers.Authorization = `Bearer ${accessToken}`;
        
        // 执行队列中的请求
        onRefreshed(accessToken);
        isRefreshing = false;
        
        return request(originalRequest);
      } catch (refreshError) {
        // 刷新token失败，清除所有token
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
        
        // 显示错误消息
        message.error('登录已过期，请重新登录');
        
        // 重置刷新状态
        isRefreshing = false;
        refreshSubscribers = [];
        
        // 跳转到登录页
        window.location.href = '/login';
        return Promise.reject(refreshError);
      }
    }
    
    // 显示错误消息
    const errorMessage = error.response?.data?.message || '请求失败';
    if (error.response?.status !== 401) {
      message.error(errorMessage);
    }
    
    return Promise.reject(error);
  }
);

export default request; 
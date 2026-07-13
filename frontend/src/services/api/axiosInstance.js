import axios from 'axios';
import { authApi } from './authApi';

const axiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request Interceptor
axiosInstance.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('access_token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

let isRefreshing = false;
let failedQueue = [];

const processQueue = (error, token = null) => {
  failedQueue.forEach(prom => {
    if (error) {
      prom.reject(error);
    } else {
      prom.resolve(token);
    }
  });
  failedQueue = [];
};

// Response Interceptor
axiosInstance.interceptors.response.use(
  (response) => {
    return response.data;
  },
  async (error) => {
    const originalRequest = error.config;

    if (error.response?.status === 401 && !originalRequest._retry) {
      if (isRefreshing) {
        return new Promise(function(resolve, reject) {
          failedQueue.push({ resolve, reject });
        }).then(token => {
          originalRequest.headers.Authorization = `Bearer ${token}`;
          return axiosInstance(originalRequest);
        }).catch(err => {
          return Promise.reject(err);
        });
      }

      originalRequest._retry = true;
      isRefreshing = true;
      const refreshToken = localStorage.getItem('refresh_token');
      
      if (refreshToken) {
        try {
          // Cannot use authApi.refreshToken directly due to circular dependency.
          // Using a separate axios instance to avoid interceptor loop.
          const res = await axios.post(`${axiosInstance.defaults.baseURL}/auth/refresh-token`, { refreshToken });
          const newAccessToken = res.data.token;
          const newRefreshToken = res.data.refreshToken;
          
          localStorage.setItem('access_token', newAccessToken);
          if (newRefreshToken) {
             localStorage.setItem('refresh_token', newRefreshToken);
          }
          
          axiosInstance.defaults.headers.common['Authorization'] = `Bearer ${newAccessToken}`;
          originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
          
          processQueue(null, newAccessToken);
          return axiosInstance(originalRequest);
        } catch (refreshError) {
          processQueue(refreshError, null);
          window.dispatchEvent(new Event('unauthorized'));
          return Promise.reject(refreshError);
        } finally {
          isRefreshing = false;
        }
      } else {
        window.dispatchEvent(new Event('unauthorized'));
      }
    }

    // Map backend ApiError to frontend
    const mappedError = error.response?.data || error;
    return Promise.reject(mappedError);
  }
);

export default axiosInstance;

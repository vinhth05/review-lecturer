import axiosInstance from './axiosInstance';

export const authApi = {
  login: (data) => axiosInstance.post('/auth/login', data),
  register: (data) => axiosInstance.post('/auth/register', data),
  verify: (data) => axiosInstance.post('/auth/verify', data),
  forgotPassword: (data) => axiosInstance.post('/auth/forgot-password', data),
  resetPassword: (data) => axiosInstance.post('/auth/reset-password', data),
  refreshToken: (data) => axiosInstance.post('/auth/refresh-token', data),
};

import axiosInstance from './axiosInstance';

export const studentApi = {
  getProfile: () => axiosInstance.get('/students/me'),
  updateProfile: (data) => axiosInstance.put('/students/me', data),
  changePassword: (data) => axiosInstance.post('/students/me/change-password', data),
};

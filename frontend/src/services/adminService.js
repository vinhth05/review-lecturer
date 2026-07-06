import api from './axios';
export const adminService = {
  getStatistics: async () => (await api.get('/admin/statistics')).data,
};
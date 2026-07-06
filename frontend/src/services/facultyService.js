import api from './axios';
export const facultyService = {
  list: async () => (await api.get('/faculties')).data,
  create: async (data) => (await api.post('/admin/faculties', data)).data,
};
import api from './axios';
export const subjectService = {
  list: async (facultyCode) => (await api.get('/subjects', { params: { facultyCode } })).data,
  create: async (data) => (await api.post('/admin/subjects', data)).data,
};
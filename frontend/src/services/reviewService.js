import api from './axios';
export const reviewService = {
  list: async (lecturerId) => (await api.get('/reviews', { params: { lecturerId } })).data,
  create: async (data) => (await api.post('/reviews', data)).data,
};
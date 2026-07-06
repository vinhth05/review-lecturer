import api from './axios';
export const metadataService = {
  getToxicKeywords: async () => (await api.get('/admin/toxic-keywords')).data,
  addToxicKeyword: async (keyword) => (await api.post('/admin/toxic-keywords', { keyword })).data,
  removeToxicKeyword: async (id) => (await api.delete(`/admin/toxic-keywords/${id}`)).data,
};
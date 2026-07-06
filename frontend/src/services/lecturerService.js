import api from './axios';

export const lecturerService = {
  list: async (facultyCode, subjectCode) => {
    const params = {};
    if (facultyCode) params.facultyCode = facultyCode;
    if (subjectCode) params.subjectCode = subjectCode;
    const response = await api.get('/lecturers', { params });
    return response.data;
  },
  listPage: async (facultyCode, subjectCode, page = 0, size = 12) => {
    const params = { page, size };
    if (facultyCode) params.facultyCode = facultyCode;
    if (subjectCode) params.subjectCode = subjectCode;
    const response = await api.get('/lecturers/page', { params });
    return response.data;
  },
  detail: async (id) => {
    const response = await api.get(`/lecturers/${id}`);
    return response.data;
  }
};

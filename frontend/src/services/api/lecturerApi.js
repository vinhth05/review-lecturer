import axiosInstance from './axiosInstance';

export const lecturerApi = {
  getLecturers: (params) => axiosInstance.get('/lecturers', { params }),
  getLecturersPage: (params) => axiosInstance.get('/lecturers/page', { params }),
  getLecturerDetails: (id) => axiosInstance.get(`/lecturers/${id}`),
};

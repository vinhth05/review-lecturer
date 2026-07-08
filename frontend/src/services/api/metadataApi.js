import axiosInstance from './axiosInstance';

export const metadataApi = {
  getFaculties: () => axiosInstance.get('/metadata/faculties'),
  getSubjects: (facultyCode) => axiosInstance.get('/metadata/subjects', { params: { facultyCode } }),
};

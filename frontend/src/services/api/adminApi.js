import axiosInstance from './axiosInstance';

export const adminApi = {
  // Statistics
  getStatistics: () => axiosInstance.get('/admin/statistics'),
  
  // Reviews
  getPendingReviews: () => axiosInstance.get('/admin/reviews/pending'),
  getReviews: (params) => axiosInstance.get('/admin/reviews', { params }),
  approveReview: (id) => axiosInstance.patch(`/admin/reviews/${id}/approve`),
  rejectReview: (id) => axiosInstance.patch(`/admin/reviews/${id}/reject`),
  deleteReview: (id) => axiosInstance.delete(`/admin/reviews/${id}`),
  bulkApproveReviews: (ids) => axiosInstance.post('/admin/reviews/bulk-approve', ids),
  bulkDeleteReviews: (ids) => axiosInstance.post('/admin/reviews/bulk-delete', ids),
  
  // Users
  getUsers: (params) => axiosInstance.get('/admin/users', { params }),
  updateUserRole: (id, data) => axiosInstance.patch(`/admin/users/${id}/role`, data),
  lockUser: (id) => axiosInstance.patch(`/admin/users/${id}/lock`),
  unlockUser: (id) => axiosInstance.patch(`/admin/users/${id}/unlock`),
  verifyUser: (id) => axiosInstance.patch(`/admin/users/${id}/verified`),
  unverifyUser: (id) => axiosInstance.patch(`/admin/users/${id}/unverify`),
  deleteUser: (id) => axiosInstance.delete(`/admin/users/${id}`),
  resetUserPassword: (id, data) => axiosInstance.patch(`/admin/users/${id}/reset-password`, data),
  exportUsers: () => axiosInstance.get('/admin/export/users', { responseType: 'blob' }),

  // Lecturers
  getLecturers: (params) => axiosInstance.get('/admin/lecturers', { params }),
  createLecturer: (data) => axiosInstance.post('/admin/lecturers', data),
  updateLecturer: (id, data) => axiosInstance.patch(`/admin/lecturers/${id}`, data),
  deleteLecturer: (id) => axiosInstance.delete(`/admin/lecturers/${id}`),
  hideLecturer: (id) => axiosInstance.patch(`/admin/lecturers/${id}/hide`),
  unhideLecturer: (id) => axiosInstance.patch(`/admin/lecturers/${id}/unhide`),
  importCtuLecturers: (data) => axiosInstance.post('/admin/lecturers/import/ctu', data),

  // Faculties
  getFaculties: (params) => axiosInstance.get('/admin/faculties', { params }),
  createFaculty: (data) => axiosInstance.post('/admin/faculties', data),
  updateFaculty: (id, data) => axiosInstance.patch(`/admin/faculties/${id}`, data),
  deleteFaculty: (id) => axiosInstance.delete(`/admin/faculties/${id}`),

  // Subjects
  getSubjects: (params) => axiosInstance.get('/admin/subjects', { params }),
  createSubject: (data) => axiosInstance.post('/admin/subjects', data),
  updateSubject: (id, data) => axiosInstance.patch(`/admin/subjects/${id}`, data),
  deleteSubject: (id) => axiosInstance.delete(`/admin/subjects/${id}`),

  // Toxic Keywords
  getToxicKeywords: () => axiosInstance.get('/admin/toxic-keywords'),
  addToxicKeyword: (data) => axiosInstance.post('/admin/toxic-keywords', data),
  updateToxicKeyword: (id, data) => axiosInstance.patch(`/admin/toxic-keywords/${id}`, data),
  deleteToxicKeyword: (id) => axiosInstance.delete(`/admin/toxic-keywords/${id}`),

  // Reports
  getReports: (params) => axiosInstance.get('/admin/reports', { params }),
  deleteReport: (id) => axiosInstance.delete(`/admin/reports/${id}`),
  bulkDeleteReports: (ids) => axiosInstance.post('/admin/reports/bulk-delete', ids),
};

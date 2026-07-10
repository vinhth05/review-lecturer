import axiosInstance from './axiosInstance';

export const reviewApi = {
  submitReview: (data) => axiosInstance.post('/reviews', data),
  reportReview: (data) => axiosInstance.post('/reports', data),
  getMyReviews: () => axiosInstance.get('/reviews/me').then(res => res),
};

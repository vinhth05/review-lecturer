import api from './axios';

export const authService = {
    login: async (credentials) => {
        const response = await api.post('/auth/login', credentials);
        return response.data;
    },
    register: async (data) => {
        const response = await api.post('/auth/register', data);
        return response.data;
    },
    verifyOtp: async (data) => {
        const response = await api.post('/auth/verify', data);
        return response.data;
    },
    forgotPassword: async (email) => {
        const response = await api.post('/auth/forgot-password', { email });
        return response.data;
    },
    resetPassword: async (data) => {
        const response = await api.post('/auth/reset-password', data);
        return response.data;
    },
    getProfile: async () => {
        // We'll define a profile endpoint if it exists, or just use the token info
        const response = await api.get('/auth/profile');
        return response.data;
    }
};

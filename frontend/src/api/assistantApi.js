import axiosInstance from './axiosInstance';

export const assistantApi = {
  getStats: async () => {
    const response = await axiosInstance.get('/dashboard/stats');
    return response.data;
  },

  getRecentQuestions: async (limit = 10) => {
    const response = await axiosInstance.get(`/dashboard/recent-questions?limit=${limit}`);
    return response.data;
  },

  regenerateToken: async () => {
    const response = await axiosInstance.post('/assistant/regenerate-token');
    return response.data;
  },
};

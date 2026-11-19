import axiosInstance from './axiosInstance';

export const chatApi = {
  askQuestion: async (assistantToken, question) => {
    const response = await axiosInstance.post('/public/ask', {
      assistantToken,
      question,
    });
    return response.data;
  },

  getAssistantPublicInfo: async (assistantToken) => {
    const response = await axiosInstance.get(`/public/assistant-info/${assistantToken}`);
    return response.data;
  },
};

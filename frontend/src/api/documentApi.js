import axiosInstance from './axiosInstance';

export const documentApi = {
  getDocuments: async () => {
    const response = await axiosInstance.get('/documents');
    return response.data;
  },

  uploadDocument: async (file) => {
    const formData = new FormData();
    formData.append('file', file);

    const response = await axiosInstance.post('/documents/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return response.data;
  },

  deleteDocument: async (id) => {
    const response = await axiosInstance.delete(`/documents/${id}`);
    return response.data;
  },
};

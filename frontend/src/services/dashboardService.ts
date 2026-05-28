import axiosInstance from '../config/axios';

export const dashboardService = {
  getSummary: async () => {
    const response = await axiosInstance.get('/api/dashboard/summary');
    return response.data;
  },
};

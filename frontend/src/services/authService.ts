import axiosInstance from '../config/axios';
import type { AuthResponse, LoginData, RegisterData, User } from '../types/auth';

export const authService = {
  login: async (data: LoginData): Promise<AuthResponse> => {
    const res = await axiosInstance.post('/api/auth/login', data);
    return res.data;
  },
  register: async (data: RegisterData) => {
    const res = await axiosInstance.post('/api/auth/register', data);
    return res.data;
  },
  getProfile: async (): Promise<User> => {
    const res = await axiosInstance.get('/api/auth/profile');
    return res.data;
  },
  updateProfile: async (data: { fullName: string; employeeId: string }) => {
    const res = await axiosInstance.put('/api/auth/profile', data);
    return res.data;
  },
  changePassword: async (data: { oldPassword: string; newPassword: string }) => {
    const res = await axiosInstance.put('/api/auth/password', data);
    return res.data;
  }
};

import axiosInstance from '../config/axios';

export interface OrderRequestItem {
  id: string;
  skuId: string;
  skuName: string;
  quantity: number;
  expectedDate?: string;
}

export interface OrderRequestResponse {
  id: string;
  code: string;
  expectedDate: string;
  notes?: string;
  status: 'DRAFT' | 'PENDING' | 'PROCESSING' | 'ORDERED' | 'CANCELLED';
  cancelReason?: string;
  createdById: string;
  createdByName: string;
  processedById?: string;
  processedByName?: string;
  itemCount: number;
  items: OrderRequestItem[];
  createdAt: string;
  updatedAt: string;
}

export interface OrderRequestItemRequest {
  skuId: string;
  quantity: number;
  expectedDate?: string;
}

export interface OrderRequestUpsertRequest {
  expectedDate: string;
  notes?: string;
  items: OrderRequestItemRequest[];
}

export const orderRequestService = {
  list: async (status?: string, search?: string): Promise<OrderRequestResponse[]> => {
    const params = new URLSearchParams();
    if (status) params.append('status', status);
    if (search) params.append('search', search);
    const response = await axiosInstance.get(`/api/order-requests?${params.toString()}`);
    return response.data;
  },
  detail: async (id: string): Promise<OrderRequestResponse> => {
    const response = await axiosInstance.get(`/api/order-requests/${id}`);
    return response.data;
  },
  create: async (data: OrderRequestUpsertRequest): Promise<OrderRequestResponse> => {
    const response = await axiosInstance.post('/api/order-requests', data);
    return response.data;
  },
  update: async (id: string, data: OrderRequestUpsertRequest): Promise<OrderRequestResponse> => {
    const response = await axiosInstance.put(`/api/order-requests/${id}`, data);
    return response.data;
  },
  submit: async (id: string): Promise<OrderRequestResponse> => {
    const response = await axiosInstance.patch(`/api/order-requests/${id}/submit`);
    return response.data;
  },
  cancel: async (id: string, reason: string): Promise<OrderRequestResponse> => {
    const response = await axiosInstance.patch(`/api/order-requests/${id}/cancel`, { reason });
    return response.data;
  }
};

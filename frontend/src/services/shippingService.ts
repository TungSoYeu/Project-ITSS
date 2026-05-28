import axiosInstance from '../config/axios';
import type { POStatus, PurchaseOrderResponse } from './purchaseOrderService';

export interface TrackingResponse {
  id: string;
  purchaseOrderId: string;
  status: POStatus;
  location?: string;
  notes?: string;
  evidenceFileUrl?: string;
  updatedById: string;
  updatedByName: string;
  timestamp: string;
}

export interface TrackingRequest {
  status: POStatus;
  location?: string;
  notes?: string;
  evidenceFileUrl?: string;
}

export const shippingService = {
  inTransit: async (): Promise<PurchaseOrderResponse[]> => {
    const response = await axiosInstance.get('/api/shipments/in-transit');
    return response.data;
  },
  history: async (purchaseOrderId: string): Promise<TrackingResponse[]> => {
    const response = await axiosInstance.get(`/api/shipments/purchase-orders/${purchaseOrderId}/tracking`);
    return response.data;
  },
  addTracking: async (purchaseOrderId: string, data: TrackingRequest): Promise<TrackingResponse> => {
    const response = await axiosInstance.post(`/api/shipments/purchase-orders/${purchaseOrderId}/tracking`, data);
    return response.data;
  },
};

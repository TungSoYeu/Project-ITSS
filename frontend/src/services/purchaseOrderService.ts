import axiosInstance from '../config/axios';
import type { TransportMethod } from './optimizationService';

export type POStatus = 'PENDING_CONFIRM' | 'PREPARING' | 'SHIPPING' | 'ARRIVED' | 'COMPLETED' | 'CANCELLED';

export interface PurchaseOrderItemResponse {
  id: string;
  skuId: string;
  skuCode: string;
  skuName: string;
  unit: string;
  quantityOrdered: number;
  quantityReceived: number;
  difference: number;
  notes?: string;
}

export interface PurchaseOrderResponse {
  id: string;
  code: string;
  requestId: string;
  requestCode: string;
  siteId: string;
  siteCode: string;
  siteName: string;
  createdById: string;
  createdByName: string;
  transportMethod: TransportMethod;
  status: POStatus;
  expectedArrivalDate: string;
  actualArrivalDate?: string;
  cancelReason?: string;
  itemCount: number;
  items: PurchaseOrderItemResponse[];
  createdAt: string;
  updatedAt: string;
}

export const purchaseOrderService = {
  list: async (status?: POStatus | '', siteId?: string, search?: string): Promise<PurchaseOrderResponse[]> => {
    const params = new URLSearchParams();
    if (status) params.append('status', status);
    if (siteId) params.append('siteId', siteId);
    if (search) params.append('search', search);
    const response = await axiosInstance.get(`/api/purchase-orders?${params.toString()}`);
    return response.data;
  },
  detail: async (id: string): Promise<PurchaseOrderResponse> => {
    const response = await axiosInstance.get(`/api/purchase-orders/${id}`);
    return response.data;
  },
  generateFromRequest: async (requestId: string): Promise<PurchaseOrderResponse[]> => {
    const response = await axiosInstance.post(`/api/order-requests/${requestId}/purchase-orders`);
    return response.data;
  },
  updateStatus: async (id: string, status: POStatus, actualArrivalDate?: string): Promise<PurchaseOrderResponse> => {
    const response = await axiosInstance.patch(`/api/purchase-orders/${id}/status`, { status, actualArrivalDate });
    return response.data;
  },
  cancel: async (id: string, reason: string): Promise<PurchaseOrderResponse> => {
    const response = await axiosInstance.patch(`/api/purchase-orders/${id}/cancel`, { reason });
    return response.data;
  },
};

import axiosInstance from '../config/axios';
import type { PurchaseOrderResponse } from './purchaseOrderService';

export interface ReceiveItemRequest {
  purchaseOrderItemId: string;
  quantityReceived: number;
  notes?: string;
}

export interface ReceivePurchaseOrderRequest {
  actualArrivalDate?: string;
  items: ReceiveItemRequest[];
}

export const warehouseService = {
  inbound: async (): Promise<PurchaseOrderResponse[]> => {
    const response = await axiosInstance.get('/api/warehouse/inbound');
    return response.data;
  },
  receive: async (purchaseOrderId: string, data: ReceivePurchaseOrderRequest): Promise<PurchaseOrderResponse> => {
    const response = await axiosInstance.post(`/api/warehouse/purchase-orders/${purchaseOrderId}/receive`, data);
    return response.data;
  },
};

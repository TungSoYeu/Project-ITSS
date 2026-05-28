import axiosInstance from '../config/axios';

export type TransportMethod = 'SEA' | 'AIR';

export interface CandidateResponse {
  skuId: string;
  skuCode: string;
  skuName: string;
  requestedQuantity: number;
  siteId: string;
  siteCode: string;
  siteName: string;
  availableQuantity: number;
  transportMethod: TransportMethod;
  leadTimeDays: number;
  expectedArrivalDate: string;
  feasible: boolean;
}

export interface AllocationResponse {
  skuId: string;
  skuCode: string;
  skuName: string;
  siteId: string;
  siteCode: string;
  siteName: string;
  transportMethod: TransportMethod;
  quantity: number;
  expectedArrivalDate: string;
}

export interface InventoryCheckResponse {
  requestId: string;
  requestCode: string;
  candidates: CandidateResponse[];
  warnings: string[];
}

export interface OptimizationResponse {
  requestId: string;
  requestCode: string;
  allocations: AllocationResponse[];
  warnings: string[];
}

export const optimizationService = {
  checkInventory: async (requestId: string): Promise<InventoryCheckResponse> => {
    const response = await axiosInstance.get(`/api/order-requests/${requestId}/inventory-check`);
    return response.data;
  },
  optimize: async (requestId: string): Promise<OptimizationResponse> => {
    const response = await axiosInstance.post(`/api/order-requests/${requestId}/optimize`);
    return response.data;
  },
};

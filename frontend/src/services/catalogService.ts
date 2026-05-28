import axiosInstance from '../config/axios';

export interface SkuResponse {
  id: string;
  code: string;
  name: string;
  description?: string;
  unit: string;
  category: string;
  active: boolean;
}

export interface SiteResponse {
  id: string;
  code: string;
  name: string;
  country: string;
  seaLeadTime: number;
  airLeadTime: number;
  active: boolean;
  skuCount: number;
  createdAt: string;
  updatedAt: string;
}

export interface SiteRequest {
  code: string;
  name: string;
  country: string;
  seaLeadTime: number;
  airLeadTime: number;
  active: boolean;
}

export interface InventoryResponse {
  id: string;
  siteId: string;
  siteCode: string;
  siteName: string;
  skuId: string;
  skuCode: string;
  skuName: string;
  unit: string;
  quantity: number;
  updatedAt: string;
}

export const catalogService = {
  listSkus: async (search?: string): Promise<SkuResponse[]> => {
    const params = new URLSearchParams();
    if (search) params.append('search', search);
    const response = await axiosInstance.get(`/api/skus?${params.toString()}`);
    return response.data;
  },
  listSites: async (active?: boolean, search?: string): Promise<SiteResponse[]> => {
    const params = new URLSearchParams();
    if (active !== undefined) params.append('active', String(active));
    if (search) params.append('search', search);
    const response = await axiosInstance.get(`/api/sites?${params.toString()}`);
    return response.data;
  },
  createSite: async (data: SiteRequest): Promise<SiteResponse> => {
    const response = await axiosInstance.post('/api/sites', data);
    return response.data;
  },
  updateSite: async (id: string, data: SiteRequest): Promise<SiteResponse> => {
    const response = await axiosInstance.put(`/api/sites/${id}`, data);
    return response.data;
  },
  getSiteInventory: async (siteId: string): Promise<InventoryResponse[]> => {
    const response = await axiosInstance.get(`/api/sites/${siteId}/inventory`);
    return response.data;
  },
  upsertInventory: async (siteId: string, skuId: string, quantity: number): Promise<InventoryResponse> => {
    const response = await axiosInstance.put(`/api/sites/${siteId}/inventory`, { skuId, quantity });
    return response.data;
  },
};

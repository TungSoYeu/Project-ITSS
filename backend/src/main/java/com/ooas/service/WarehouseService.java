package com.ooas.service;

import com.ooas.dto.PurchaseOrderResponse;
import com.ooas.dto.ReceivePurchaseOrderRequest;
import java.util.List;

public interface WarehouseService {
    List<PurchaseOrderResponse> inbound();
    PurchaseOrderResponse receive(String purchaseOrderId, ReceivePurchaseOrderRequest request);
}

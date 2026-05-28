package com.ooas.service;

import com.ooas.entity.POStatus;
import com.ooas.entity.PurchaseOrder;
import com.ooas.dto.CancelRequest;
import com.ooas.dto.PurchaseOrderResponse;
import com.ooas.dto.UpdatePoStatusRequest;
import java.util.List;

public interface PurchaseOrderService {
    List<PurchaseOrderResponse> list(POStatus status, String siteId, String search);
    PurchaseOrderResponse detail(String id);
    List<PurchaseOrderResponse> generateFromRequest(String requestId, @org.springframework.lang.NonNull String userId);
    PurchaseOrderResponse updateStatus(String id, UpdatePoStatusRequest request);
    PurchaseOrderResponse cancel(String id, CancelRequest request);
    PurchaseOrder requirePurchaseOrder(String id);
}

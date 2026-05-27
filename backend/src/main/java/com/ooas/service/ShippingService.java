package com.ooas.service;

import com.ooas.dto.PurchaseOrderResponse;
import com.ooas.dto.TrackingRequest;
import com.ooas.dto.TrackingResponse;
import java.util.List;

public interface ShippingService {
    List<PurchaseOrderResponse> inTransit();
    List<TrackingResponse> history(String purchaseOrderId);
    TrackingResponse addTracking(String purchaseOrderId, TrackingRequest request, @org.springframework.lang.NonNull String userId);
}

package com.ooas.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record PurchaseOrderResponse(
        String id,
        String code,
        String requestId,
        String requestCode,
        String siteId,
        String siteCode,
        String siteName,
        String createdById,
        String createdByName,
        TransportMethod transportMethod,
        POStatus status,
        LocalDate expectedArrivalDate,
        LocalDate actualArrivalDate,
        String cancelReason,
        int itemCount,
        List<PurchaseOrderItemResponse> items,
        Instant createdAt,
        Instant updatedAt
) {
}

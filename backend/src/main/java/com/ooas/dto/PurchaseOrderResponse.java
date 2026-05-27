package com.ooas.dto;

import com.ooas.entity.POStatus;
import com.ooas.entity.PurchaseOrder;
import com.ooas.entity.TransportMethod;
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
    public static PurchaseOrderResponse from(PurchaseOrder po, boolean includeItems) {
        List<PurchaseOrderItemResponse> itemResponses = includeItems
                ? po.getItems().stream().map(PurchaseOrderItemResponse::from).toList()
                : List.of();
        return new PurchaseOrderResponse(
                po.getId(),
                po.getCode(),
                po.getRequest().getId(),
                po.getRequest().getCode(),
                po.getSite().getId(),
                po.getSite().getCode(),
                po.getSite().getName(),
                po.getCreatedBy().getId(),
                po.getCreatedBy().getFullName(),
                po.getTransportMethod(),
                po.getStatus(),
                po.getExpectedArrivalDate(),
                po.getActualArrivalDate(),
                po.getCancelReason(),
                po.getItems().size(),
                itemResponses,
                po.getCreatedAt(),
                po.getUpdatedAt()
        );
    }
}

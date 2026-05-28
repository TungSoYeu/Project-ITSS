package com.ooas.dto;

import com.ooas.entity.RequestStatus;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record OrderRequestResponse(
        String id,
        String code,
        LocalDate expectedDate,
        String notes,
        RequestStatus status,
        String cancelReason,
        String createdById,
        String createdByName,
        String processedById,
        String processedByName,
        int itemCount,
        List<OrderRequestItemResponse> items,
        Instant createdAt,
        Instant updatedAt
) {
    public static OrderRequestResponse from(OrderRequest request, boolean includeItems) {
        List<OrderRequestItemResponse> itemResponses = includeItems
                ? request.getItems().stream().map(OrderRequestItemResponse::from).toList()
                : List.of();
        return new OrderRequestResponse(
                request.getId(),
                request.getCode(),
                request.getExpectedDate(),
                request.getNotes(),
                request.getStatus(),
                request.getCancelReason(),
                request.getCreatedBy().getId(),
                request.getCreatedBy().getFullName(),
                request.getProcessedBy() == null ? null : request.getProcessedBy().getId(),
                request.getProcessedBy() == null ? null : request.getProcessedBy().getFullName(),
                request.getItems().size(),
                itemResponses,
                request.getCreatedAt(),
                request.getUpdatedAt()
        );
    }
}

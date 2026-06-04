package com.ooas.desktop.shared.model;

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
}


package com.ooas.desktop.shared.model;

import java.time.LocalDateTime;

public record SiteInquiryItemResponse(
        String id,
        String inquiryId,
        String skuId,
        String skuCode,
        String skuName,
        int quantityRequested,
        int quantityAvailable,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}

package com.ooas.desktop.shared.model;

import java.time.Instant;

public record InventoryResponse(
        String id,
        String siteId,
        String siteCode,
        String siteName,
        String skuId,
        String skuCode,
        String skuName,
        String unit,
        int quantity,
        Instant updatedAt
) {
}


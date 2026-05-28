package com.ooas.dto;

import com.ooas.entity.SiteInventory;
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
    public static InventoryResponse from(SiteInventory inventory) {
        return new InventoryResponse(
                inventory.getId(),
                inventory.getSite().getId(),
                inventory.getSite().getCode(),
                inventory.getSite().getName(),
                inventory.getSku().getId(),
                inventory.getSku().getCode(),
                inventory.getSku().getName(),
                inventory.getSku().getUnit(),
                inventory.getQuantity(),
                inventory.getUpdatedAt()
        );
    }
}

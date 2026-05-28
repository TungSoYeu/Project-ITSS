package com.ooas.dto;

import com.ooas.entity.Sku;
import java.time.Instant;

public record SkuResponse(
        String id,
        String code,
        String name,
        String unit,
        String description,
        Instant createdAt,
        Instant updatedAt
) {
    public static SkuResponse from(Sku sku) {
        return new SkuResponse(
                sku.getId(),
                sku.getCode(),
                sku.getName(),
                sku.getUnit(),
                sku.getDescription(),
                sku.getCreatedAt(),
                sku.getUpdatedAt()
        );
    }
}

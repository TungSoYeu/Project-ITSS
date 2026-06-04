package com.ooas.desktop.shared.model;

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
}


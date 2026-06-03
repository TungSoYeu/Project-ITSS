package com.ooas.model;

import java.time.Instant;

public record SiteResponse(
        String id,
        String code,
        String name,
        String country,
        int seaLeadTime,
        int airLeadTime,
        boolean active,
        long skuCount,
        Instant createdAt,
        Instant updatedAt
) {
}

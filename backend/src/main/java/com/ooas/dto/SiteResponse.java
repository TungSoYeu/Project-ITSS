package com.ooas.dto;

import com.ooas.entity.Site;
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
    public static SiteResponse from(Site site, long skuCount) {
        return new SiteResponse(
                site.getId(),
                site.getCode(),
                site.getName(),
                site.getCountry(),
                site.getSeaLeadTime(),
                site.getAirLeadTime(),
                site.isActive(),
                skuCount,
                site.getCreatedAt(),
                site.getUpdatedAt()
        );
    }
}

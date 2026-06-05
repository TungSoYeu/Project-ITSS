package com.ooas.desktop.shared.model;

import java.time.LocalDateTime;
import java.util.List;

public record SiteInquiryResponse(
        String id,
        String requestId,
        String siteId,
        String siteName,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<SiteInquiryItemResponse> items
) {
}

package com.ooas.desktop.shared.model;

import java.time.LocalDate;

public record AllocationResponse(
        String skuId,
        String skuCode,
        String skuName,
        String siteId,
        String siteCode,
        String siteName,
        TransportMethod transportMethod,
        int quantity,
        LocalDate expectedArrivalDate
) {
}


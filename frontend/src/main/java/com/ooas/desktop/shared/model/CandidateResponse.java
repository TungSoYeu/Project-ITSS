package com.ooas.desktop.shared.model;

import java.time.LocalDate;

public record CandidateResponse(
        String skuId,
        String skuCode,
        String skuName,
        int requestedQuantity,
        String siteId,
        String siteCode,
        String siteName,
        int availableQuantity,
        TransportMethod transportMethod,
        int leadTimeDays,
        LocalDate expectedArrivalDate,
        boolean feasible
) {
}


package com.ooas.optimization.dto;

import com.ooas.domain.TransportMethod;
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

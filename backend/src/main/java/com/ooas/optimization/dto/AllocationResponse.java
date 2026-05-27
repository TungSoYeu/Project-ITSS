package com.ooas.optimization.dto;

import com.ooas.domain.TransportMethod;
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

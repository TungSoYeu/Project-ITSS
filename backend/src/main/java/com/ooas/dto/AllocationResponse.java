package com.ooas.dto;

import com.ooas.entity.TransportMethod;
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

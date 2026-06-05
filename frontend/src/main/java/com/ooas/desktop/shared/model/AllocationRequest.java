package com.ooas.desktop.shared.model;

import java.time.LocalDate;

public record AllocationRequest(
        String skuId,
        String siteId,
        TransportMethod transportMethod,
        int quantity,
        LocalDate expectedArrivalDate
) {
}

package com.ooas.desktop.shared.model;

import java.time.LocalDate;

public record OrderRequestItemResponse(
        String id,
        String skuId,
        String skuCode,
        String skuName,
        String unit,
        int quantity,
        LocalDate expectedDate
) {
}


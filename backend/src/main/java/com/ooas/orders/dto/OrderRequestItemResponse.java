package com.ooas.orders.dto;

import com.ooas.domain.OrderRequestItem;
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
    public static OrderRequestItemResponse from(OrderRequestItem item) {
        return new OrderRequestItemResponse(
                item.getId(),
                item.getSku().getId(),
                item.getSku().getCode(),
                item.getSku().getName(),
                item.getSku().getUnit(),
                item.getQuantity(),
                item.getExpectedDate()
        );
    }
}

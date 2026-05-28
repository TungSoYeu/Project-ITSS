package com.ooas.dto;

import com.ooas.entity.PurchaseOrderItem;

public record PurchaseOrderItemResponse(
        String id,
        String skuId,
        String skuCode,
        String skuName,
        String unit,
        int quantityOrdered,
        int quantityReceived,
        int difference,
        String notes
) {
    public static PurchaseOrderItemResponse from(PurchaseOrderItem item) {
        return new PurchaseOrderItemResponse(
                item.getId(),
                item.getSku().getId(),
                item.getSku().getCode(),
                item.getSku().getName(),
                item.getSku().getUnit(),
                item.getQuantityOrdered(),
                item.getQuantityReceived(),
                item.getDifference(),
                item.getNotes()
        );
    }
}

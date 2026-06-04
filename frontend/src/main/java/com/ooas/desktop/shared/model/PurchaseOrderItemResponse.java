package com.ooas.desktop.shared.model;

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
}


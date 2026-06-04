package com.ooas.desktop.shared.model;

public record ReceiveItemRequest(String purchaseOrderItemId, int quantityReceived, String notes) {
}


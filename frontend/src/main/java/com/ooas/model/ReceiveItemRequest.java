package com.ooas.model;

public record ReceiveItemRequest(String purchaseOrderItemId, int quantityReceived, String notes) {
}

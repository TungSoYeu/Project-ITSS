package com.ooas.warehouse.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record ReceiveItemRequest(
        @NotBlank(message = "PO item id khong duoc de trong")
        String purchaseOrderItemId,

        @Min(value = 0, message = "So luong thuc nhan khong duoc am")
        int quantityReceived,

        String notes
) {
}

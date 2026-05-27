package com.ooas.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record InventoryRequest(
        @NotBlank(message = "SKU id khong duoc de trong")
        String skuId,

        @Min(value = 0, message = "Ton kho khong duoc am")
        int quantity
) {
}

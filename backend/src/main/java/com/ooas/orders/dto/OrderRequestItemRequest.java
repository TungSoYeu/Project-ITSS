package com.ooas.orders.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public record OrderRequestItemRequest(
        @NotBlank(message = "SKU id khong duoc de trong")
        String skuId,

        @Min(value = 1, message = "So luong phai lon hon 0")
        int quantity,

        LocalDate expectedDate
) {
}

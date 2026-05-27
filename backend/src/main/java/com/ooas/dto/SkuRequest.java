package com.ooas.dto;

import jakarta.validation.constraints.NotBlank;

public record SkuRequest(
        @NotBlank(message = "Ma SKU khong duoc de trong")
        String code,

        @NotBlank(message = "Ten SKU khong duoc de trong")
        String name,

        @NotBlank(message = "Don vi tinh khong duoc de trong")
        String unit,

        String description
) {
}

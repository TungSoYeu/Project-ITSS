package com.ooas.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record SiteRequest(
        @NotBlank(message = "Ma site khong duoc de trong")
        String code,

        @NotBlank(message = "Ten site khong duoc de trong")
        String name,

        @NotBlank(message = "Quoc gia khong duoc de trong")
        String country,

        @Min(value = 0, message = "Sea lead time khong duoc am")
        int seaLeadTime,

        @Min(value = 0, message = "Air lead time khong duoc am")
        int airLeadTime,

        Boolean active
) {
}

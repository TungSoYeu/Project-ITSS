package com.ooas.catalog.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record SiteRequest(
        @NotBlank(message = "Ma site khong duoc de trong")
        String code,

        @NotBlank(message = "Ten site khong duoc de trong")
        String name,

        @NotBlank(message = "Quoc gia khong duoc de trong")
        String country,

        @Min(value = 1, message = "Sea lead time phai lon hon 0")
        int seaLeadTime,

        @Min(value = 1, message = "Air lead time phai lon hon 0")
        int airLeadTime,

        Boolean active
) {
}

package com.ooas.dto;

import jakarta.validation.constraints.NotBlank;

public record CancelRequest(
        @NotBlank(message = "Ly do huy khong duoc de trong")
        String reason
) {
}

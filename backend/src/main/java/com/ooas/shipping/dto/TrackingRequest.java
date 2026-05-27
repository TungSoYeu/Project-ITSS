package com.ooas.shipping.dto;

import com.ooas.domain.POStatus;
import jakarta.validation.constraints.NotNull;

public record TrackingRequest(
        @NotNull(message = "Trang thai van chuyen khong hop le")
        POStatus status,
        String location,
        String notes,
        String evidenceFileUrl
) {
}

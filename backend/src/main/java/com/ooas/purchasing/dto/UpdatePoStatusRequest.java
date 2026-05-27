package com.ooas.purchasing.dto;

import com.ooas.domain.POStatus;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record UpdatePoStatusRequest(
        @NotNull(message = "Trang thai PO khong hop le")
        POStatus status,
        LocalDate actualArrivalDate
) {
}

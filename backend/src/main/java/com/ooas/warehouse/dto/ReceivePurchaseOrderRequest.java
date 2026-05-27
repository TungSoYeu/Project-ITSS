package com.ooas.warehouse.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.time.LocalDate;
import java.util.List;

public record ReceivePurchaseOrderRequest(
        LocalDate actualArrivalDate,

        @NotEmpty(message = "Can co danh sach hang thuc nhan")
        List<@Valid ReceiveItemRequest> items
) {
}

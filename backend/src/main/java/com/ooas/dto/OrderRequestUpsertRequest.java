package com.ooas.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

public record OrderRequestUpsertRequest(
        @NotNull(message = "Ngay can nhan khong duoc de trong")
        @Future(message = "Ngay can nhan phai lon hon ngay hien tai")
        LocalDate expectedDate,

        String notes,

        @NotEmpty(message = "Yeu cau phai co it nhat mot SKU")
        List<@Valid OrderRequestItemRequest> items
) {
}

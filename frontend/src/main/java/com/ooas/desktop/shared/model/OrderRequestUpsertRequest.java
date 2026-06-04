package com.ooas.desktop.shared.model;

import java.time.LocalDate;
import java.util.List;

public record OrderRequestUpsertRequest(LocalDate expectedDate, String notes, List<OrderRequestItemRequest> items) {
}


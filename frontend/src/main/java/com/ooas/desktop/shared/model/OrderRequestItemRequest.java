package com.ooas.desktop.shared.model;

import java.time.LocalDate;

public record OrderRequestItemRequest(String skuId, int quantity, LocalDate expectedDate) {
}


package com.ooas.desktop.shared.model;

import java.time.LocalDate;
import java.util.List;

public record ReceivePurchaseOrderRequest(LocalDate actualArrivalDate, List<ReceiveItemRequest> items) {
}


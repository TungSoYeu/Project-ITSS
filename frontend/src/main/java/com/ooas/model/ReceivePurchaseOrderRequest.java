package com.ooas.model;

import java.time.LocalDate;
import java.util.List;

public record ReceivePurchaseOrderRequest(LocalDate actualArrivalDate, List<ReceiveItemRequest> items) {
}

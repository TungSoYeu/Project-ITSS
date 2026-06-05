package com.ooas.desktop.warehouse.application;

import com.ooas.desktop.warehouse.domain.InspectionLine;
import com.ooas.desktop.warehouse.domain.InspectionLogic;
import com.ooas.desktop.shared.model.PurchaseOrderResponse;
import com.ooas.desktop.shared.model.ReceiveItemRequest;
import com.ooas.desktop.shared.model.ReceivePurchaseOrderRequest;
import java.time.LocalDate;
import java.util.List;

public final class InspectionSubmitter {
    private InspectionSubmitter() {
    }

    public static void submit(WarehouseCoordinator coordinator, PurchaseOrderResponse order,
                              LocalDate arrival, List<InspectionLine> lines) {
        ReceivePurchaseOrderRequest request = new ReceivePurchaseOrderRequest(arrival,
                lines.stream().map(line -> new ReceiveItemRequest(
                        line.item().id(), line.quantity().getValue(),
                        InspectionLogic.note(line))).toList());
        coordinator.receive(order, request);
    }
}



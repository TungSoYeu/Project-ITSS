package com.ooas.desktop.warehouse.ui.page;

import com.ooas.desktop.warehouse.application.InspectionSubmitter;
import com.ooas.desktop.warehouse.application.WarehouseCoordinator;
import com.ooas.desktop.warehouse.domain.InspectionLine;
import com.ooas.desktop.warehouse.domain.InspectionLogic;
import com.ooas.desktop.warehouse.ui.component.InspectionCardFactory;
import com.ooas.desktop.warehouse.ui.component.WarehouseOrderInfo;
import com.ooas.desktop.warehouse.ui.component.WarehouseUi;

import com.ooas.desktop.shared.model.PurchaseOrderResponse;
import java.time.LocalDate;
import java.util.List;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public final class WarehouseDiscrepancyPage {
    private final WarehouseCoordinator coordinator;
    private final PurchaseOrderResponse order;
    private final LocalDate arrival;
    private final List<InspectionLine> lines;
    private final Label result;

    public WarehouseDiscrepancyPage(WarehouseCoordinator coordinator, PurchaseOrderResponse order,
                                    LocalDate arrival, List<InspectionLine> lines) {
        this.coordinator = coordinator;
        this.order = order;
        this.arrival = arrival;
        this.lines = lines;
        result = new Label(InspectionLogic.summary(lines));
        result.getStyleClass().add("inspection-result-warning");
    }

    public void show() {
        VBox page = WarehouseUi.page("Xử lý sai lệch đơn " + order.code());
        Button back = WarehouseUi.secondaryButton("Quay lại kiểm hàng");
        Button recheck = WarehouseUi.secondaryButton("Kiểm tra lại sai lệch");
        Button confirm = WarehouseUi.primaryButton("Xác nhận hoàn tất");
        back.setOnAction(event -> new WarehouseInspectionPage(coordinator, order).show());
        recheck.setOnAction(event -> recheck());
        confirm.setOnAction(event -> InspectionSubmitter.submit(coordinator, order, arrival, lines));
        VBox rows = new VBox(10);
        lines.stream().filter(InspectionLogic::hasIssue)
                .map(InspectionCardFactory::discrepancy).forEach(rows.getChildren()::add);
        page.getChildren().addAll(
                WarehouseUi.toolbar(back, recheck, confirm),
                WarehouseUi.section("Thông tin đơn hàng và sai lệch",
                        WarehouseOrderInfo.discrepancy(order, arrival, result)),
                WarehouseUi.section("Xử lý từng mặt hàng sai lệch", rows));
        coordinator.show(page, true);
    }

    private void recheck() {
        lines.forEach(InspectionLogic::update);
        boolean mismatch = lines.stream().anyMatch(InspectionLogic::hasIssue);
        result.setText(InspectionLogic.summary(lines));
        result.getStyleClass().setAll(mismatch ? "inspection-result-warning" : "inspection-result-success");
    }
}



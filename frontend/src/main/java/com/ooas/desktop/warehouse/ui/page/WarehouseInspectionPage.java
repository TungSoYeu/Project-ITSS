package com.ooas.desktop.warehouse.ui.page;

import com.ooas.desktop.warehouse.application.InspectionSubmitter;
import com.ooas.desktop.warehouse.application.WarehouseCoordinator;
import com.ooas.desktop.warehouse.domain.InspectionLine;
import com.ooas.desktop.warehouse.domain.InspectionLogic;
import com.ooas.desktop.warehouse.ui.component.InspectionCardFactory;
import com.ooas.desktop.warehouse.ui.component.WarehouseOrderInfo;
import com.ooas.desktop.warehouse.ui.component.WarehouseUi;

import com.ooas.desktop.shared.model.PurchaseOrderItemResponse;
import com.ooas.desktop.shared.model.PurchaseOrderResponse;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public final class WarehouseInspectionPage {
    private final WarehouseCoordinator coordinator;
    private final PurchaseOrderResponse order;
    private final List<InspectionLine> lines = new ArrayList<>();
    private final DatePicker arrival;
    private final Label result = new Label("Chưa đối chiếu kết quả kiểm nhận");
    private final Button confirm = WarehouseUi.primaryButton("Xác nhận nhập kho");

    public WarehouseInspectionPage(WarehouseCoordinator coordinator, PurchaseOrderResponse order) {
        this.coordinator = coordinator;
        this.order = order;
        arrival = new DatePicker(order.actualArrivalDate() == null ? LocalDate.now() : order.actualArrivalDate());
        result.getStyleClass().add("inspection-result-pending");
    }

    public void show() {
        VBox page = WarehouseUi.page("Xác nhận nhập kho " + order.code());
        Button cancel = WarehouseUi.secondaryButton("Hủy");
        Button compare = WarehouseUi.secondaryButton("Đối chiếu");
        cancel.setOnAction(event -> new WarehouseOrderDetailPage(coordinator, order).show());
        compare.setOnAction(event -> compare());
        confirm.setOnAction(event -> {
            compare();
            InspectionSubmitter.submit(coordinator, order, arrival.getValue(), lines);
        });
        VBox rows = new VBox(10);
        for (PurchaseOrderItemResponse item : items()) {
            InspectionLine line = InspectionCardFactory.create(item);
            lines.add(line);
            rows.getChildren().add(line.card());
        }
        page.getChildren().addAll(
                WarehouseUi.toolbar(cancel, compare, confirm),
                WarehouseUi.section("Thông tin đơn hàng", WarehouseOrderInfo.inspection(order, arrival, result)),
                WarehouseUi.section("Hàng thực nhận", rows));
        coordinator.show(page, true);
    }

    private void compare() {
        lines.forEach(InspectionLogic::update);
        boolean mismatch = lines.stream().anyMatch(InspectionLogic::hasIssue);
        result.setText(mismatch
                ? InspectionLogic.summary(lines) + ". Hệ thống sẽ lưu trạng thái Có chênh lệch/Cần xử lý."
                : "Đối chiếu khớp: số lượng, tình trạng hàng và bao bì đều đạt.");
        result.getStyleClass().setAll(mismatch ? "inspection-result-warning" : "inspection-result-success");
    }

    private List<PurchaseOrderItemResponse> items() {
        return order.items() == null ? List.of() : order.items();
    }
}



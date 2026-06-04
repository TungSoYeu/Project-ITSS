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
    private final Label result = new Label("Chưa kiểm tra đối chiếu");
    private final Button confirm = WarehouseUi.primaryButton("Xác nhận hoàn tất");

    public WarehouseInspectionPage(WarehouseCoordinator coordinator, PurchaseOrderResponse order) {
        this.coordinator = coordinator;
        this.order = order;
        arrival = new DatePicker(order.actualArrivalDate() == null ? LocalDate.now() : order.actualArrivalDate());
        result.getStyleClass().add("inspection-result-pending");
        confirm.setDisable(true);
    }

    public void show() {
        VBox page = WarehouseUi.page("Kiểm hàng đơn " + order.code());
        Button back = WarehouseUi.secondaryButton("Quay lại chi tiết");
        Button compare = WarehouseUi.primaryButton("Kiểm tra đối chiếu");
        back.setOnAction(event -> new WarehouseOrderDetailPage(coordinator, order).show());
        compare.setOnAction(event -> compare());
        confirm.setOnAction(event -> InspectionSubmitter.submit(coordinator, order, arrival.getValue(), lines));
        VBox rows = new VBox(10);
        for (PurchaseOrderItemResponse item : items()) {
            InspectionLine line = InspectionCardFactory.create(item);
            lines.add(line);
            rows.getChildren().add(line.card());
        }
        page.getChildren().addAll(
                WarehouseUi.toolbar(back, compare, confirm),
                WarehouseUi.section("Thông tin đơn hàng kiểm kê", WarehouseOrderInfo.inspection(order, arrival, result)),
                WarehouseUi.section("Đối chiếu hàng thực nhận", rows));
        coordinator.show(page, true);
    }

    private void compare() {
        lines.forEach(InspectionLogic::update);
        lines.forEach(line -> InspectionLogic.disableInspection(line, true));
        boolean mismatch = lines.stream().anyMatch(InspectionLogic::hasIssue);
        result.setText(mismatch
                ? InspectionLogic.summary(lines) + ". Chuyển sang trang xử lý sai lệch."
                : "Đối chiếu chính xác: số lượng, tình trạng hàng và bao bì đều đạt.");
        result.getStyleClass().setAll(mismatch ? "inspection-result-warning" : "inspection-result-success");
        confirm.setDisable(mismatch);
        if (mismatch) {
            new WarehouseDiscrepancyPage(coordinator, order, arrival.getValue(), lines).show();
        }
    }

    private List<PurchaseOrderItemResponse> items() {
        return order.items() == null ? List.of() : order.items();
    }
}



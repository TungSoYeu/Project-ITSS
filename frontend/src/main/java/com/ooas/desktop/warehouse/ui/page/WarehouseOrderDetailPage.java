package com.ooas.desktop.warehouse.ui.page;

import com.ooas.desktop.warehouse.application.WarehouseCoordinator;
import com.ooas.desktop.warehouse.ui.component.WarehouseOrderInfo;
import com.ooas.desktop.warehouse.ui.component.WarehouseTables;
import com.ooas.desktop.warehouse.ui.component.WarehouseUi;

import com.ooas.desktop.shared.model.PurchaseOrderItemResponse;
import com.ooas.desktop.shared.model.PurchaseOrderResponse;
import java.util.List;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;

public final class WarehouseOrderDetailPage {
    private final WarehouseCoordinator coordinator;
    private final PurchaseOrderResponse order;

    public WarehouseOrderDetailPage(WarehouseCoordinator coordinator, PurchaseOrderResponse order) {
        this.coordinator = coordinator;
        this.order = order;
    }

    public void show() {
        VBox page = WarehouseUi.page("Chi tiết đơn hàng " + order.code());
        Button back = WarehouseUi.secondaryButton("Quay lại danh sách");
        Button inspect = WarehouseUi.primaryButton("Kiểm hàng");
        back.setOnAction(event -> coordinator.showOrderList());
        inspect.setDisable(!coordinator.canInspect(order));
        inspect.setOnAction(event -> coordinator.openInspection(order));
        TableView<PurchaseOrderItemResponse> items = WarehouseTables.itemTable();
        items.getItems().setAll(order.items() == null ? List.of() : order.items());
        items.setPrefHeight(Math.min(500, Math.max(230, 70 + items.getItems().size() * 76)));
        page.getChildren().addAll(
                WarehouseUi.toolbar(back, inspect),
                WarehouseUi.section("Thông tin đơn hàng", WarehouseOrderInfo.detail(order)),
                WarehouseUi.section("Danh sách mặt hàng", items));
        coordinator.show(page, true);
    }
}



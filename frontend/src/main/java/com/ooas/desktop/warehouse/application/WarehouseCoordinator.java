package com.ooas.desktop.warehouse.application;

import com.ooas.desktop.warehouse.ui.page.WarehouseInspectionPage;
import com.ooas.desktop.warehouse.ui.page.WarehouseOrderDetailPage;
import com.ooas.desktop.warehouse.ui.page.WarehouseOrderListPage;

import com.ooas.desktop.shared.model.POStatus;
import com.ooas.desktop.shared.model.PurchaseOrderResponse;
import com.ooas.desktop.shared.model.ReceivePurchaseOrderRequest;
import com.ooas.desktop.shared.api.DatabaseClient;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.StackPane;

public final class WarehouseCoordinator {
    private final DatabaseClient database;
    private final Runnable logout;
    private final StackPane content;
    private final WarehouseAsync async;

    public WarehouseCoordinator(DatabaseClient database, Runnable logout, StackPane content, Label status) {
        this.database = database;
        this.logout = logout;
        this.content = content;
        this.async = new WarehouseAsync(status, logout);
    }

    public void showOrderList() {
        new WarehouseOrderListPage(this).show();
    }

    public void openDetail(PurchaseOrderResponse order) {
        call("Đang tải chi tiết đơn hàng...", database.purchaseOrderDetail(order.id()),
                value -> new WarehouseOrderDetailPage(this, value).show());
    }

    public void openInspection(PurchaseOrderResponse order) {
        if (!canInspect(order)) {
            alert(Alert.AlertType.WARNING, "Không thể kiểm hàng",
                    "Chỉ kiểm hàng đơn đang vận chuyển hoặc đã đến kho.");
            return;
        }
        call("Đang tải dữ liệu kiểm hàng...", database.purchaseOrderDetail(order.id()),
                value -> new WarehouseInspectionPage(this, value).show());
    }

    public void receive(PurchaseOrderResponse order, ReceivePurchaseOrderRequest request) {
        call("Đang xác nhận kết quả kiểm hàng...", database.receivePurchaseOrder(order.id(), request), saved -> {
            alert(Alert.AlertType.INFORMATION, "Xác nhận thành công",
                    "Đã hoàn tất kiểm hàng đơn " + saved.code() + ".");
            showOrderList();
        });
    }

    public void show(Node page, boolean scrollable) {
        if (!scrollable) {
            content.getChildren().setAll(page);
            return;
        }
        ScrollPane scroll = new ScrollPane(page);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("warehouse-page-scroll");
        content.getChildren().setAll(scroll);
    }

    public <T> void call(String message, CompletableFuture<T> future, Consumer<T> success) {
        async.call(message, future, success);
    }

    public DatabaseClient database() {
        return database;
    }

    public boolean canInspect(PurchaseOrderResponse order) {
        return order.status() == POStatus.SHIPPING || order.status() == POStatus.ARRIVED;
    }

    public void alert(Alert.AlertType type, String title, String message) {
        async.alert(type, title, message);
    }
}



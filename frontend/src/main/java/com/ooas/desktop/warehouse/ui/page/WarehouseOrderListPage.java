package com.ooas.desktop.warehouse.ui.page;

import com.ooas.desktop.warehouse.application.WarehouseCoordinator;
import com.ooas.desktop.warehouse.domain.WarehouseOrderFormat;
import com.ooas.desktop.warehouse.ui.component.WarehouseEvents;
import com.ooas.desktop.warehouse.ui.component.WarehouseOrderActionCell;
import com.ooas.desktop.warehouse.ui.component.WarehouseTables;
import com.ooas.desktop.warehouse.ui.component.WarehouseUi;

import com.ooas.desktop.shared.model.PurchaseOrderResponse;
import java.util.List;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public final class WarehouseOrderListPage {
    private final WarehouseCoordinator coordinator;
    private final ObservableList<PurchaseOrderResponse> source = FXCollections.observableArrayList();
    private final TableView<PurchaseOrderResponse> table = new TableView<>();
    private final TextField search = new TextField();
    private final ComboBox<String> status = new ComboBox<>(FXCollections.observableArrayList(
            "Tất cả trạng thái", "Chưa kiểm hàng", "Đã hoàn tất kiểm hàng"));

    public WarehouseOrderListPage(WarehouseCoordinator coordinator) {
        this.coordinator = coordinator;
    }

    public void show() {
        VBox page = WarehouseUi.page("Danh sách đơn hàng");
        configureFilters();
        configureTable();
        page.getChildren().addAll(WarehouseUi.toolbar(search, status), table);
        VBox.setVgrow(table, Priority.ALWAYS);
        coordinator.show(page, false);
        coordinator.call("Đang tải danh sách đơn nhập...",
                coordinator.database().listPurchaseOrders(null, null, ""), orders -> {
                    source.setAll(orders);
                    filter();
                });
    }

    private void configureFilters() {
        search.setPromptText("Tìm mã đơn hàng hoặc site");
        search.setPrefWidth(280);
        status.setValue("Tất cả trạng thái");
        status.setPrefWidth(190);
        search.setOnAction(event -> filter());
        status.setOnAction(event -> filter());
    }

    private void configureTable() {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.getColumns().setAll(List.of(
                WarehouseTables.indexColumn(),
                WarehouseTables.column("Mã đơn hàng", 145, PurchaseOrderResponse::code),
                WarehouseTables.column("Site", 190, row -> row.siteCode() + " - " + row.siteName()),
                WarehouseTables.column("Ngày dự kiến", 125, PurchaseOrderResponse::expectedArrivalDate),
                WarehouseTables.column("Số mặt hàng", 110, PurchaseOrderResponse::itemCount),
                WarehouseTables.column("Trạng thái kiểm hàng", 180, WarehouseOrderFormat::inspectionStatus),
                WarehouseTables.column("Kết quả sai lệch", 180, WarehouseOrderFormat::discrepancySummary),
                actionColumn()));
        table.setOnMouseClicked(event -> {
            PurchaseOrderResponse selected = table.getSelectionModel().getSelectedItem();
            if (event.getClickCount() == 1 && selected != null && !WarehouseEvents.isMenuTarget(event.getTarget())) {
                coordinator.openDetail(selected);
            }
        });
    }

    private void filter() {
        String keyword = search.getText() == null ? "" : search.getText().trim().toLowerCase();
        table.getItems().setAll(source.stream()
                .filter(WarehouseOrderFormat::visibleInWarehouse)
                .filter(order -> "Tất cả trạng thái".equals(status.getValue())
                        || WarehouseOrderFormat.inspectionStatus(order).equals(status.getValue()))
                .filter(order -> keyword.isBlank() || searchable(order).contains(keyword))
                .toList());
    }

    private String searchable(PurchaseOrderResponse order) {
        return (order.code() + " " + order.siteCode() + " " + order.siteName()).toLowerCase();
    }

    private TableColumn<PurchaseOrderResponse, Void> actionColumn() {
        TableColumn<PurchaseOrderResponse, Void> column = new TableColumn<>("Thao tác");
        column.setPrefWidth(120);
        column.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>());
        column.setCellFactory(ignored -> new WarehouseOrderActionCell(coordinator));
        return column;
    }
}



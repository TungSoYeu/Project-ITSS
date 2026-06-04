package com.ooas.desktop.warehouse.ui.component;

import com.ooas.desktop.warehouse.application.WarehouseCoordinator;

import com.ooas.desktop.shared.model.PurchaseOrderResponse;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableCell;

public final class WarehouseOrderActionCell extends TableCell<PurchaseOrderResponse, Void> {
    private final WarehouseCoordinator coordinator;
    private final MenuItem detail = new MenuItem("Xem chi tiết");
    private final MenuItem inspect = new MenuItem("Kiểm hàng");
    private final MenuButton menu = new MenuButton("Thao tác", null, detail, inspect);

    public WarehouseOrderActionCell(WarehouseCoordinator coordinator) {
        this.coordinator = coordinator;
        menu.getStyleClass().add("warehouse-action-menu");
        detail.setOnAction(event -> coordinator.openDetail(order()));
        inspect.setOnAction(event -> coordinator.openInspection(order()));
    }

    @Override
    protected void updateItem(Void item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || getIndex() >= getTableView().getItems().size()) {
            setGraphic(null);
            return;
        }
        inspect.setDisable(!coordinator.canInspect(order()));
        setGraphic(menu);
    }

    private PurchaseOrderResponse order() {
        return getTableView().getItems().get(getIndex());
    }
}



package com.ooas.desktop.warehouse.ui.component;

import com.ooas.desktop.warehouse.domain.WarehouseOrderFormat;

import com.ooas.desktop.shared.model.PurchaseOrderResponse;
import java.time.LocalDate;
import javafx.scene.Node;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

public final class WarehouseOrderInfo {
    private WarehouseOrderInfo() {
    }

    public static Node detail(PurchaseOrderResponse order) {
        GridPane grid = base(order);
        WarehouseUi.addInfo(grid, 3, 0, "Ngày đến thực tế", order.actualArrivalDate());
        WarehouseUi.addInfo(grid, 3, 2, "Trạng thái kiểm hàng", WarehouseOrderFormat.inspectionStatus(order));
        WarehouseUi.addInfo(grid, 4, 0, "Kết quả sai lệch", WarehouseOrderFormat.discrepancySummary(order));
        WarehouseUi.addInfo(grid, 4, 2, "Người tạo", order.createdByName());
        WarehouseUi.addInfo(grid, 5, 0, "Ngày tạo", order.createdAt());
        WarehouseUi.addInfo(grid, 5, 2, "Lý do hủy", order.cancelReason());
        return grid;
    }

    public static Node inspection(PurchaseOrderResponse order, DatePicker actualArrival, Label result) {
        GridPane grid = base(order);
        Label arrivalLabel = key("Ngày đến thực tế");
        grid.add(arrivalLabel, 0, 3);
        grid.add(actualArrival, 1, 3);
        grid.add(key("Kết quả đối chiếu"), 2, 3);
        grid.add(result, 3, 3);
        return grid;
    }

    public static Node discrepancy(PurchaseOrderResponse order, LocalDate actualArrival, Label result) {
        GridPane grid = base(order);
        WarehouseUi.addInfo(grid, 3, 0, "Ngày đến thực tế", actualArrival);
        grid.add(key("Kết quả sai lệch"), 2, 3);
        grid.add(result, 3, 3);
        return grid;
    }

    private static GridPane base(PurchaseOrderResponse order) {
        GridPane grid = WarehouseUi.grid();
        WarehouseUi.addInfo(grid, 0, 0, "Mã đơn hàng", order.code());
        WarehouseUi.addInfo(grid, 0, 2, "Mã yêu cầu", order.requestCode());
        WarehouseUi.addInfo(grid, 1, 0, "Site", order.siteCode() + " - " + order.siteName());
        WarehouseUi.addInfo(grid, 1, 2, "Xuất xứ", order.siteCountry());
        WarehouseUi.addInfo(grid, 2, 0, "Phương thức vận chuyển", order.transportMethod());
        WarehouseUi.addInfo(grid, 2, 2, "Ngày dự kiến", order.expectedArrivalDate());
        return grid;
    }

    private static Label key(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("warehouse-info-key");
        return label;
    }
}



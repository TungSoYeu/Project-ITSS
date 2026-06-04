package com.ooas.desktop.warehouse.ui.component;

import com.ooas.desktop.warehouse.domain.InspectionLine;
import com.ooas.desktop.warehouse.domain.InspectionLogic;

import com.ooas.desktop.shared.model.PurchaseOrderItemResponse;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

public final class InspectionCardFactory {
    private InspectionCardFactory() {
    }

    public static InspectionLine create(PurchaseOrderItemResponse item) {
        InspectionLine line = new InspectionLine(item, WarehouseUi.spinner(item.quantityOrdered()),
                InspectionFields.condition(), InspectionFields.packaging(), new TextField(), result(),
                InspectionFields.reason(), InspectionFields.resolution(), new TextField(), card());
        line.inspectionNotes().setPromptText("Ghi chú kiểm kê thực tế");
        Button edit = WarehouseUi.secondaryButton("Chỉnh sửa");
        edit.setOnAction(event -> InspectionLogic.disableInspection(line, false));
        GridPane card = line.card();
        card.add(title(item), 0, 0, 4, 1);
        card.add(new Label("Số lượng đặt: " + item.quantityOrdered() + " " + item.unit()), 0, 1);
        card.add(WarehouseUi.labeled("Số lượng thực nhận", line.quantity()), 1, 1);
        card.add(WarehouseUi.labeled("Tình trạng hàng", line.condition()), 2, 1);
        card.add(WarehouseUi.labeled("Tình trạng bao bì", line.packaging()), 3, 1);
        card.add(WarehouseUi.labeled("Ghi chú kiểm kê", line.inspectionNotes()), 0, 2, 3, 1);
        card.add(WarehouseUi.labeled("Kết quả", line.difference()), 3, 2);
        card.add(WarehouseUi.toolbar(edit), 0, 3, 4, 1);
        return line;
    }

    public static GridPane discrepancy(InspectionLine line) {
        InspectionLogic.disableInspection(line, true);
        InspectionLogic.disableResolution(line, true);
        GridPane card = card();
        card.getStyleClass().add("inspection-card-mismatch");
        Button edit = WarehouseUi.secondaryButton("Chỉnh sửa");
        edit.setOnAction(event -> {
            InspectionLogic.disableInspection(line, false);
            InspectionLogic.disableResolution(line, false);
        });
        card.add(title(line.item()), 0, 0, 4, 1);
        card.add(new Label("Số lượng đặt: " + line.item().quantityOrdered() + " " + line.item().unit()), 0, 1);
        card.add(WarehouseUi.labeled("Điều chỉnh số lượng thực tế", line.quantity()), 1, 1);
        card.add(WarehouseUi.labeled("Tình trạng hàng", line.condition()), 2, 1);
        card.add(WarehouseUi.labeled("Tình trạng bao bì", line.packaging()), 3, 1);
        card.add(WarehouseUi.labeled("Lý do sai lệch", line.reason()), 0, 2);
        card.add(WarehouseUi.labeled("Phương án xử lý", line.resolution()), 1, 2);
        card.add(WarehouseUi.labeled("Ghi chú xử lý", line.resolutionNotes()), 2, 2);
        card.add(WarehouseUi.labeled("Sai lệch hiện tại", line.difference()), 3, 2);
        card.add(WarehouseUi.toolbar(edit), 0, 3, 4, 1);
        return card;
    }

    private static GridPane card() {
        GridPane card = new GridPane();
        card.setHgap(12);
        card.setVgap(10);
        card.getStyleClass().add("inspection-card");
        return card;
    }

    private static Label title(PurchaseOrderItemResponse item) {
        Label title = new Label(item.skuCode() + " - " + item.skuName());
        title.getStyleClass().add("inspection-item-title");
        return title;
    }

    private static Label result() {
        Label result = new Label("Chưa kiểm tra");
        result.getStyleClass().add("difference-pending");
        return result;
    }
}



package com.ooas.desktop.warehouse.ui.component;

import com.ooas.desktop.warehouse.domain.InspectionLine;

import com.ooas.desktop.shared.model.PurchaseOrderItemResponse;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

public final class InspectionCardFactory {
    private InspectionCardFactory() {
    }

    public static InspectionLine create(PurchaseOrderItemResponse item) {
        InspectionLine line = new InspectionLine(item, WarehouseUi.spinner(item.quantityOrdered()),
                InspectionFields.condition(), InspectionFields.packaging(), new TextField(), result(),
                InspectionFields.reason(), InspectionFields.resolution(), new TextField(), card());
        line.inspectionNotes().setPromptText("Ghi chú nếu có chênh lệch");
        GridPane card = line.card();
        card.add(title(item), 0, 0, 5, 1);
        card.add(WarehouseUi.labeled("SL đặt", new Label(Integer.toString(item.quantityOrdered()))), 0, 1);
        card.add(WarehouseUi.labeled("Đơn vị", new Label(item.unit())), 1, 1);
        card.add(WarehouseUi.labeled("SL nhận", line.quantity()), 2, 1);
        card.add(WarehouseUi.labeled("Kết quả", line.difference()), 3, 1);
        card.add(WarehouseUi.labeled("Ghi chú", line.inspectionNotes()), 4, 1);
        return line;
    }

    private static GridPane card() {
        GridPane card = new GridPane();
        card.setHgap(16);
        card.setVgap(8);
        card.setMaxWidth(980);
        card.getColumnConstraints().addAll(
                column(90, false),
                column(110, false),
                column(150, false),
                column(180, false),
                column(360, true)
        );
        card.getStyleClass().add("inspection-card");
        return card;
    }

    private static ColumnConstraints column(double width, boolean grow) {
        ColumnConstraints constraints = new ColumnConstraints();
        constraints.setPrefWidth(width);
        if (grow) {
            constraints.setHgrow(Priority.ALWAYS);
        }
        return constraints;
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



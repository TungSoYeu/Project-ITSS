package com.ooas.desktop.warehouse.ui.component;

import com.ooas.desktop.shared.model.PurchaseOrderItemResponse;
import java.util.List;
import java.util.function.Function;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public final class WarehouseTables {
    private WarehouseTables() {
    }

    public static <T> TableColumn<T, String> column(String title, int width, Function<T, Object> extractor) {
        TableColumn<T, String> column = new TableColumn<>(title);
        column.setPrefWidth(width);
        column.setCellValueFactory(data -> new ReadOnlyStringWrapper(WarehouseUi.format(extractor.apply(data.getValue()))));
        return column;
    }

    public static <T> TableColumn<T, String> indexColumn() {
        TableColumn<T, String> column = new TableColumn<>("STT");
        column.setPrefWidth(55);
        column.setCellValueFactory(data -> new ReadOnlyStringWrapper(""));
        column.setCellFactory(ignored -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : Integer.toString(getIndex() + 1));
            }
        });
        return column;
    }

    public static TableView<PurchaseOrderItemResponse> itemTable() {
        TableView<PurchaseOrderItemResponse> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.getColumns().setAll(List.of(
                column("Mã hàng", 120, PurchaseOrderItemResponse::skuCode),
                column("Tên hàng", 240, row -> row.skuName() + " (" + row.unit() + ")"),
                column("SL đặt", 100, PurchaseOrderItemResponse::quantityOrdered),
                column("SL nhận", 100, PurchaseOrderItemResponse::quantityReceived),
                column("Chênh lệch", 130, WarehouseTables::difference),
                wrappedColumn("Ghi chú kiểm nhận", 360, PurchaseOrderItemResponse::notes)
        ));
        table.setMinHeight(210);
        table.getStyleClass().add("warehouse-item-table");
        return table;
    }

    private static <T> TableColumn<T, String> wrappedColumn(String title, int width, Function<T, Object> extractor) {
        TableColumn<T, String> column = column(title, width, extractor);
        column.setCellFactory(ignored -> new TableCell<>() {
            private final Label text = new Label();
            {
                text.setWrapText(true);
                text.setMaxWidth(Double.MAX_VALUE);
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                text.setText(empty || item == null ? "" : item);
                setGraphic(empty ? null : text);
            }
        });
        return column;
    }

    private static String difference(PurchaseOrderItemResponse item) {
        return item.difference() == 0 ? "Khớp"
                : item.difference() < 0 ? "Thiếu " + Math.abs(item.difference()) : "Thừa " + item.difference();
    }
}



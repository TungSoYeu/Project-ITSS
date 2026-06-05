package com.ooas.desktop.warehouse.application;

import com.ooas.desktop.warehouse.ui.component.WarehouseUi;

import com.ooas.desktop.shared.model.UserResponse;
import com.ooas.desktop.shared.api.DatabaseClient;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public final class WarehouseWorkspace {
    private final DatabaseClient database;
    private final UserResponse user;
    private final Runnable logout;

    public WarehouseWorkspace(DatabaseClient database, UserResponse user, Runnable logout) {
        this.database = database;
        this.user = user;
        this.logout = logout;
    }

    public Parent createRoot() {
        StackPane content = new StackPane();
        Label status = new Label("Sẵn sàng");
        WarehouseCoordinator coordinator = new WarehouseCoordinator(database, logout, content, status);
        BorderPane root = new BorderPane();
        root.getStyleClass().add("warehouse-shell");
        root.setLeft(sidebar(coordinator));
        content.getStyleClass().add("warehouse-content");
        root.setCenter(content);
        status.getStyleClass().add("warehouse-status-bar");
        status.setMaxWidth(Double.MAX_VALUE);
        root.setBottom(status);
        coordinator.showOrderList();
        return root;
    }

    private VBox sidebar(WarehouseCoordinator coordinator) {
        VBox nav = new VBox(8);
        nav.getStyleClass().add("warehouse-sidebar");
        nav.setPrefWidth(250);
        Label brand = new Label("WMS");
        brand.getStyleClass().add("warehouse-brand");
        Label subtitle = new Label("Hệ thống quản lý kho");
        subtitle.getStyleClass().add("warehouse-subtitle");
        Label account = new Label(user.fullName() + "\n" + user.employeeId());
        account.getStyleClass().add("warehouse-sidebar-user");
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        nav.getChildren().addAll(brand, subtitle, account, new Separator(),
                WarehouseUi.navButton("Danh sách đơn hàng", coordinator::showOrderList),
                spacer, WarehouseUi.navButton("Đăng xuất", logout));
        return nav;
    }
}



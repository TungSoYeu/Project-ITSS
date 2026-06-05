package com.ooas.desktop.application;

import com.ooas.desktop.shared.exception.DatabaseException;
import com.ooas.desktop.shared.model.*;
import com.ooas.desktop.shared.api.DatabaseClient;
import com.ooas.desktop.warehouse.application.WarehouseWorkspace;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.prefs.Preferences;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;

public class OoasDesktopApplication extends Application {
    private static final String PREF_SESSION_USER_ID = "sessionUserId";
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final ButtonType SAVE_BUTTON = new ButtonType("Lưu", ButtonBar.ButtonData.OK_DONE);

    private final Preferences preferences = Preferences.userNodeForPackage(OoasDesktopApplication.class);
    private DatabaseClient database;
    private Scene scene;
    private StackPane content;
    private Label statusLabel;
    private UserResponse currentUser;
    private String currentToken;

    public static void main(String[] args) {
        if ("Asia/Saigon".equals(java.util.TimeZone.getDefault().getID())) {
            java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        }
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        database = new DatabaseClient(defaultJdbcUrl());
        scene = new Scene(new StackPane(), 1360, 820);
        String stylesheet = getClass().getResource("/com/ooas/desktop/ooas/app.css").toExternalForm();
        scene.getStylesheets().add(stylesheet);
        scene.getStylesheets().add(getClass().getResource("/com/ooas/desktop/warehouse/warehouse.css").toExternalForm());
        scene.getStylesheets().add(getClass().getResource("/com/ooas/desktop/warehouse/warehouse-controls.css").toExternalForm());
        scene.getStylesheets().add(getClass().getResource("/com/ooas/desktop/warehouse/warehouse-inspection.css").toExternalForm());

        stage.setTitle("OOAS JavaFX");
        stage.setMinWidth(1040);
        stage.setMinHeight(680);
        stage.setScene(scene);
        stage.show();

        String savedUserId = preferences.get(PREF_SESSION_USER_ID, "");
        if (!savedUserId.isBlank()) {
            currentToken = savedUserId;
            database.setToken(savedUserId);
            database.profile().whenComplete((user, error) -> Platform.runLater(() -> {
                if (error != null) {
                    preferences.remove(PREF_SESSION_USER_ID);
                    alert(Alert.AlertType.WARNING, "Khôi phục phiên thất bại", errorMessage(error));
                    showLogin();
                    return;
                }
                currentUser = user;
                showWorkspace();
            }));
        } else {
            showLogin();
        }
    }

    private String defaultJdbcUrl() {
        String fromEnv = System.getenv("DB_URL");
        return fromEnv == null || fromEnv.isBlank() ? "jdbc:postgresql://localhost:5432/ooas" : fromEnv;
    }

    private void showLogin() {
        currentUser = null;
        currentToken = null;
        database.setToken(null);

        BorderPane root = new BorderPane();
        root.getStyleClass().add("login-shell");

        VBox panel = new VBox(14);
        panel.getStyleClass().add("login-panel");
        panel.setMaxWidth(420);

        Label title = new Label("OOAS JavaFX");
        title.getStyleClass().add("login-title");
        Label subtitle = new Label("Phần mềm quản lý đặt hàng quốc tế");
        subtitle.getStyleClass().add("muted");

        TextField email = new TextField();
        email.setPromptText("Email");
        PasswordField password = new PasswordField();
        password.setPromptText("Mật khẩu");

        Label message = new Label();
        message.getStyleClass().add("error-label");

        Button loginButton = primaryButton("Đăng nhập");
        loginButton.setDefaultButton(true);
        Button registerButton = secondaryButton("Đăng ký tài khoản");

        loginButton.setOnAction(event -> {
            if (email.getText().isBlank() || password.getText().isBlank()) {
                message.setText("Vui lòng nhập Email và Mật khẩu.");
                return;
            }
            message.setText("Đang đăng nhập...");
            loginButton.setDisable(true);
            database.login(email.getText().trim(), password.getText())
                    .whenComplete((auth, error) -> Platform.runLater(() -> {
                        loginButton.setDisable(false);
                        if (error != null) {
                            message.setText(errorMessage(error));
                            return;
                        }
                        currentToken = auth.accessToken();
                        currentUser = auth.user();
                        database.setToken(currentToken);
                        preferences.put(PREF_SESSION_USER_ID, currentToken);
                        showWorkspace();
                    }));
        });
        registerButton.setOnAction(event -> {
            showRegisterDialog();
        });

        panel.getChildren().addAll(
                title,
                subtitle,
                labeled("Email", email),
                labeled("Mật khẩu", password),
                message,
                new HBox(10, loginButton, registerButton)
        );

        StackPane center = new StackPane(panel);
        center.setPadding(new Insets(32));
        root.setCenter(center);
        scene.setRoot(root);
    }

    private void showRegisterDialog() {
        Dialog<RegisterRequest> dialog = new Dialog<>();
        dialog.setTitle("Đăng ký tài khoản");
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType(), ButtonType.CANCEL);

        TextField fullName = new TextField();
        TextField email = new TextField();
        PasswordField password = new PasswordField();
        TextField employeeId = new TextField();
        ComboBox<Role> role = enumCombo(Role.class);
        role.setValue(Role.SALES);

        GridPane form = formGrid();
        form.addRow(0, new Label("Họ và tên"), fullName);
        form.addRow(1, new Label("Email"), email);
        form.addRow(2, new Label("Mật khẩu"), password);
        form.addRow(3, new Label("Mã nhân viên"), employeeId);
        form.addRow(4, new Label("Vai trò"), role);

        Node save = dialog.getDialogPane().lookupButton(saveButtonType());
        save.addEventFilter(ActionEvent.ACTION, event -> {
            if (fullName.getText().isBlank() || email.getText().isBlank() || password.getText().isBlank() || employeeId.getText().isBlank()) {
                alert(Alert.AlertType.WARNING, "Thiếu dữ liệu", "Vui lòng điền đủ các trường bắt buộc.");
                event.consume();
            }
        });
        dialog.getDialogPane().setContent(form);
        dialog.setResultConverter(type -> type == saveButtonType()
                ? new RegisterRequest(fullName.getText().trim(), email.getText().trim(), password.getText(), employeeId.getText().trim(), role.getValue())
                : null);

        dialog.showAndWait().ifPresent(request -> call("Đang đăng ký tài khoản...", database.register(request), result -> {
            alert(Alert.AlertType.INFORMATION, "Đã gửi đăng ký", "Yêu cầu đăng ký đã được gửi. Vui lòng chờ quản trị viên phê duyệt.");
            showLogin();
        }));
    }

    private void showWorkspace() {
        if (currentUser != null && currentUser.role() == Role.WAREHOUSE) {
            showWarehouseSystem();
            return;
        }
        showMain();
    }

    private void showMain() {
        if (scene.getWindow() instanceof Stage stage) {
            stage.setTitle("OOAS JavaFX");
        }
        BorderPane root = new BorderPane();
        root.getStyleClass().add("app-shell");

        content = new StackPane();
        content.getStyleClass().add("content");

        VBox nav = new VBox(8);
        nav.getStyleClass().add("sidebar");
        nav.setPrefWidth(230);

        Label brand = new Label("OOAS");
        brand.getStyleClass().add("brand");
        Label user = new Label(currentUser == null ? "" : currentUser.fullName() + "\n" + currentUser.role());
        user.getStyleClass().add("sidebar-user");

        List<Node> navItems = new java.util.ArrayList<>();
        navItems.add(brand);
        navItems.add(user);
        navItems.add(new Separator());
        navItems.add(navButton("Tổng quan", this::showDashboard));
        
        if (currentUser != null) {
            switch (currentUser.role()) {
                case ADMIN -> {
                    navItems.add(navButton("Quản lý tài khoản", this::showUsers));
                    navItems.add(navButton("Cơ sở & Tồn kho", this::showSites));
                }
                case SALES -> {
                    navItems.add(navButton("Yêu cầu đặt hàng", this::showOrderRequests));
                    navItems.add(navButton("Cơ sở & Tồn kho", this::showSites));
                }
                case OVERSEAS_ORDER -> {
                    navItems.add(navButton("Yêu cầu đặt hàng", this::showOrderRequests));
                    navItems.add(navButton("Đơn đặt hàng (PO)", this::showPurchaseOrders));
                    navItems.add(navButton("Theo dõi vận chuyển", this::showShipments));
                }
                case SITE -> {
                    navItems.add(navButton("Yêu cầu đặt hàng", this::showOrderRequests));
                    navItems.add(navButton("Đơn đặt hàng (PO)", this::showPurchaseOrders));
                    navItems.add(navButton("Tồn kho của tôi", this::showMyInventory));
                }
                default -> {}
            }
        }
        
        navItems.add(navButton("Hồ sơ cá nhân", this::showProfile));
        navItems.add(spacer());
        navItems.add(navButton("Đăng xuất", this::logout));
        
        nav.getChildren().addAll(navItems);

        statusLabel = new Label("Sẵn sàng");
        statusLabel.getStyleClass().add("status-bar");
        statusLabel.setMaxWidth(Double.MAX_VALUE);

        root.setLeft(nav);
        root.setCenter(content);
        root.setBottom(statusLabel);
        scene.setRoot(root);
        showDashboard();
    }

    private void showWarehouseSystem() {
        if (scene.getWindow() instanceof Stage stage) {
            stage.setTitle("Warehouse Management System");
        }
        scene.setRoot(new WarehouseWorkspace(database, currentUser, this::logout).createRoot());
    }

    private void logout() {
        preferences.remove(PREF_SESSION_USER_ID);
        showLogin();
    }

    private void showDashboard() {
        VBox page = page("Tổng quan");
        Button refresh = secondaryButton("Làm mới");
        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.setVgap(14);
        for (int i = 0; i < 4; i++) {
            ColumnConstraints constraints = new ColumnConstraints();
            constraints.setPercentWidth(25);
            grid.getColumnConstraints().add(constraints);
        }
        page.getChildren().addAll(toolbar(refresh), grid);
        setPage(page);

        Runnable load = () -> call("Đang tải tổng quan...", database.dashboardSummary(), summary -> {
            grid.getChildren().clear();
            grid.add(metric("Yêu cầu chờ duyệt", summary.getOrDefault("pendingRequests", 0L)), 0, 0);
            grid.add(metric("Yêu cầu đang xử lý", summary.getOrDefault("processingRequests", 0L)), 1, 0);
            grid.add(metric("Đơn hàng đang vận chuyển", summary.getOrDefault("shippingOrders", 0L)), 2, 0);
            grid.add(metric("Hàng sắp về kho", summary.getOrDefault("warehouseInboundOrders", 0L)), 3, 0);
        });
        refresh.setOnAction(event -> load.run());
        load.run();
    }

    private void showOrderRequests() {
        VBox page = page("Yêu cầu đặt hàng");

        ComboBox<String> statusFilter = enumFilter(RequestStatus.class, "Tất cả trạng thái");
        TextField search = new TextField();
        search.setPromptText("Tìm theo mã hoặc người tạo");
        Button refresh = secondaryButton("Làm mới");
        Button create = primaryButton("Tạo yêu cầu mới");
        Button edit = secondaryButton("Sửa");
        Button detail = secondaryButton("Chi tiết");
        Button cancel = secondaryButton("Hủy");
        Button sendInquiry = secondaryButton("Gửi yêu cầu tới Site");
        Button replyInquiry = primaryButton("Kiểm tra & Phản hồi");
        Button generatePo = secondaryButton("Tạo PO");

        TableView<OrderRequestResponse> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.getColumns().setAll(List.of(
                column("Mã", 130, OrderRequestResponse::code),
                column("Trạng thái", 120, OrderRequestResponse::status),
                column("Dự kiến", 110, OrderRequestResponse::expectedDate),
                column("Số lượng SP", 110, OrderRequestResponse::itemCount),
                column("Tạo bởi", 170, OrderRequestResponse::createdByName),
                column("Xử lý bởi", 170, OrderRequestResponse::processedByName),
                column("Cập nhật", 150, OrderRequestResponse::updatedAt)
        ));
        VBox.setVgrow(table, Priority.ALWAYS);

        Runnable load = () -> call("Đang tải yêu cầu đặt hàng...", database.listOrderRequests(enumSelection(statusFilter, RequestStatus.class, "Tất cả trạng thái"), search.getText()), table.getItems()::setAll);
        refresh.setOnAction(event -> load.run());
        search.setOnAction(event -> load.run());
        create.setOnAction(event -> openOrderDialog(null, load));
        detail.setOnAction(event -> withSelected(table, row -> call("Đang tải chi tiết yêu cầu...", 
            database.orderRequestDetail(row.id()).thenCombineAsync(database.getSiteInquiries(row.id(), null), (req, inq) -> {
                String sentTo = inq.isEmpty() ? null : inq.stream().map(com.ooas.desktop.shared.model.SiteInquiryResponse::siteName).collect(java.util.stream.Collectors.joining(", "));
                return new Object[]{req, sentTo};
            }), 
            result -> showOrderRequestDetail((OrderRequestResponse) result[0], (String) result[1])
        )));
        edit.setOnAction(event -> withSelected(table, row -> {
            if (row.status() != RequestStatus.PENDING) {
                alert(Alert.AlertType.WARNING, "Không thể sửa", "Chỉ có thể sửa yêu cầu ở trạng thái PENDING.");
                return;
            }
            openOrderDialog(row, load);
        }));
        cancel.setOnAction(event -> withReason("Hủy yêu cầu", reason -> withSelected(table, row -> call("Đang hủy yêu cầu...", database.cancelOrderRequest(row.id(), reason), updated -> load.run()))));
        sendInquiry.setOnAction(event -> withSelected(table, row -> showSendInquiryDialog(row, load)));
        replyInquiry.setOnAction(event -> withSelected(table, row -> replySiteInquiry(row.id(), load)));
        generatePo.setOnAction(event -> withSelected(table, row -> showGeneratePoDialog(row, load)));

        List<Node> tbNodes = new java.util.ArrayList<>();
        tbNodes.add(statusFilter);
        tbNodes.add(search);
        tbNodes.add(refresh);
        if (currentUser.role() == Role.SALES) {
            tbNodes.add(create);
            tbNodes.add(edit);
            tbNodes.add(detail);
            tbNodes.add(cancel);
        } else if (currentUser.role() == Role.OVERSEAS_ORDER) {
            tbNodes.add(detail);
            tbNodes.add(sendInquiry);
            tbNodes.add(generatePo);
            tbNodes.add(cancel);
        } else if (currentUser.role() == Role.SITE) {
            tbNodes.add(detail);
            tbNodes.add(replyInquiry);
        }

        page.getChildren().addAll(
                toolbar(tbNodes.toArray(new Node[0])),
                table
        );
        setPage(page);
        load.run();
    }

    private void replySiteInquiry(String requestId, Runnable onComplete) {
        call("Đang tải yêu cầu...", database.getSiteInquiries(requestId, currentUser.siteId()), inquiries -> {
            if (inquiries.isEmpty()) {
                alert(Alert.AlertType.WARNING, "Lỗi", "Không tìm thấy yêu cầu hỏi hàng nào cho Site của bạn.");
                return;
            }
            SiteInquiryResponse inquiry = inquiries.get(0);
            if (!"PENDING".equals(inquiry.status())) {
                alert(Alert.AlertType.WARNING, "Lỗi", "Yêu cầu này đã được phản hồi (Trạng thái: " + inquiry.status() + ").");
                return;
            }

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Phản hồi yêu cầu");
            confirm.setHeaderText("Xác nhận khả năng cung cấp");
            confirm.setContentText("Bạn có đồng ý cung cấp cho yêu cầu này không?");

            ButtonType acceptBtn = new ButtonType("Đồng ý", ButtonBar.ButtonData.YES);
            ButtonType rejectBtn = new ButtonType("Từ chối", ButtonBar.ButtonData.NO);
            ButtonType cancelBtn = new ButtonType("Hủy", ButtonBar.ButtonData.CANCEL_CLOSE);

            confirm.getButtonTypes().setAll(acceptBtn, rejectBtn, cancelBtn);

            confirm.showAndWait().ifPresent(type -> {
                if (type == acceptBtn) {
                    call("Đang xử lý phản hồi...", database.replyInquiry(inquiry.id(), true), ignored -> {
                        alert(Alert.AlertType.INFORMATION, "Thành công", "Đã ĐỒNG Ý phản hồi kiểm tra tồn kho cho OVS.");
                        onComplete.run();
                    });
                } else if (type == rejectBtn) {
                    call("Đang xử lý phản hồi...", database.replyInquiry(inquiry.id(), false), ignored -> {
                        alert(Alert.AlertType.INFORMATION, "Thành công", "Đã TỪ CHỐI phản hồi kiểm tra tồn kho cho OVS.");
                        onComplete.run();
                    });
                }
            });
        });
    }

    private void showSendInquiryDialog(OrderRequestResponse request, Runnable onComplete) {
        Dialog<List<AllocationRequest>> dialog = new Dialog<>();
        dialog.setTitle("Xác định kho cho yêu cầu " + request.code());
        dialog.getDialogPane().getButtonTypes().addAll(new ButtonType("Gửi", ButtonBar.ButtonData.OK_DONE), ButtonType.CANCEL);

        TableView<AllocationRequest> allocTable = new TableView<>();
        allocTable.getColumns().setAll(List.of(
                column("Mã SKU", 100, AllocationRequest::skuId),
                column("Site ID", 100, AllocationRequest::siteId),
                column("Phương tiện", 100, AllocationRequest::transportMethod),
                column("Số lượng", 100, AllocationRequest::quantity),
                column("Ngày nhận", 100, AllocationRequest::expectedArrivalDate)
        ));
        allocTable.setPrefHeight(200);

        Button btnOptimize = secondaryButton("Tối ưu hóa kho");
        btnOptimize.setOnAction(e -> call("Đang tính toán...", database.optimize(request.id()), opt -> {
            if (!opt.warnings().isEmpty()) {
                alert(Alert.AlertType.WARNING, "Cảnh báo", String.join("\n", opt.warnings()));
            }
            allocTable.getItems().setAll(opt.allocations().stream().map(a -> new AllocationRequest(a.skuId(), a.siteId(), a.transportMethod(), a.quantity(), a.expectedArrivalDate())).toList());
        }));

        javafx.scene.layout.VBox box = new javafx.scene.layout.VBox(10, btnOptimize, allocTable);
        dialog.getDialogPane().setContent(box);

        dialog.setResultConverter(type -> type.getButtonData() == ButtonBar.ButtonData.OK_DONE ? new java.util.ArrayList<>(allocTable.getItems()) : null);
        dialog.showAndWait().ifPresent(allocs -> {
            if (allocs.isEmpty()) {
                alert(Alert.AlertType.WARNING, "Lỗi", "Chưa có dữ liệu phân bổ. Vui lòng Tối ưu hóa trước.");
                return;
            }
            call("Đang gửi yêu cầu...", database.sendInquiries(request.id(), allocs), result -> {
                alert(Alert.AlertType.INFORMATION, "Thành công", "Đã gửi yêu cầu hỏi hàng tới các Site.");
                onComplete.run();
            });
        });
    }

    private void showGeneratePoDialog(OrderRequestResponse request, Runnable onComplete) {
        Dialog<List<AllocationRequest>> dialog = new Dialog<>();
        dialog.setTitle("Tạo PO cho " + request.code());
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType(), ButtonType.CANCEL);

        TableView<AllocationRequest> allocTable = new TableView<>();
        allocTable.getColumns().setAll(List.of(
                column("Mã SKU", 100, AllocationRequest::skuId),
                column("Site ID", 100, AllocationRequest::siteId),
                column("Phương tiện", 100, AllocationRequest::transportMethod),
                column("Số lượng", 100, AllocationRequest::quantity),
                column("Ngày nhận", 100, AllocationRequest::expectedArrivalDate)
        ));
        allocTable.setPrefHeight(200);

        call("Đang tải dữ liệu từ Site...", database.getAllocationsFromInquiries(request.id()), allocs -> {
            if (allocs.isEmpty()) {
                alert(Alert.AlertType.WARNING, "Cảnh báo", "Không có nguồn cung cấp nào từ các site (chưa phản hồi hoặc không có hàng).");
            }
            allocTable.getItems().setAll(allocs);
        });

        javafx.scene.layout.VBox box = new javafx.scene.layout.VBox(10, allocTable);
        dialog.getDialogPane().setContent(box);

        dialog.setResultConverter(type -> type == saveButtonType() ? new java.util.ArrayList<>(allocTable.getItems()) : null);
        dialog.showAndWait().ifPresent(allocs -> {
            if (allocs.isEmpty()) {
                alert(Alert.AlertType.WARNING, "Lỗi", "Không có dữ liệu để tạo PO.");
                return;
            }
            call("Đang tạo PO...", database.generatePurchaseOrders(request.id(), allocs), result -> {
                alert(Alert.AlertType.INFORMATION, "Thành công", "Đã tạo " + result.size() + " POs.");
                onComplete.run();
            });
        });
    }

    private void openOrderDialog(OrderRequestResponse existing, Runnable afterSave) {
        call("Đang tải danh sách SKU...", database.listSkus(""), skus -> {
            Optional<OrderRequestUpsertRequest> request = showOrderDialog(skus, existing);
            request.ifPresent(value -> {
                CompletableFuture<OrderRequestResponse> future = existing == null
                        ? database.createOrderRequest(value)
                        : database.updateOrderRequest(existing.id(), value);
                call(existing == null ? "Đang tạo yêu cầu..." : "Đang cập nhật yêu cầu...", future, saved -> afterSave.run());
            });
        });
    }

    private Optional<OrderRequestUpsertRequest> showOrderDialog(List<SkuResponse> skus, OrderRequestResponse existing) {
        Dialog<OrderRequestUpsertRequest> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Yêu cầu đặt hàng mới" : "Chỉnh sửa yêu cầu");
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType(), ButtonType.CANCEL);

        DatePicker orderDate = new DatePicker(existing == null ? LocalDate.now() : existing.expectedDate());
        TextArea notes = new TextArea(existing == null ? "" : safe(existing.notes()));
        notes.setPrefRowCount(3);

        ObservableList<OrderLineDraft> rows = FXCollections.observableArrayList();
        if (existing != null && existing.items() != null) {
            existing.items().forEach(item -> skus.stream()
                    .filter(sku -> sku.id().equals(item.skuId()))
                    .findFirst()
                    .ifPresent(sku -> rows.add(new OrderLineDraft(sku, item.quantity()))));
        }

        ComboBox<SkuResponse> sku = skuCombo(skus);
        Spinner<Integer> quantity = integerSpinner(1, 1, 1_000_000);
        Button addLine = secondaryButton("Thêm SP");
        addLine.setOnAction(event -> {
            if (sku.getValue() == null) {
                alert(Alert.AlertType.WARNING, "Chưa chọn SKU", "Vui lòng chọn một SKU.");
                return;
            }
            rows.add(new OrderLineDraft(sku.getValue(), quantity.getValue()));
            sku.setValue(null);
            quantity.getValueFactory().setValue(1);
        });

        TableView<OrderLineDraft> table = new TableView<>(rows);
        table.setPrefHeight(180);
        table.getColumns().setAll(List.of(
                column("SKU", 220, row -> row.sku().code() + " - " + row.sku().name()),
                column("SL", 100, OrderLineDraft::quantity)
        ));
        Button removeLine = secondaryButton("Xóa SP");
        removeLine.setOnAction(event -> {
            OrderLineDraft selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                rows.remove(selected);
            }
        });

        GridPane form = formGrid();
        form.addRow(0, new Label("Ngày đặt"), orderDate);
        form.addRow(1, new Label("Ghi chú"), notes);

        VBox box = new VBox(12,
                form,
                new Separator(),
                new Label("Số lượng SP"),
                toolbar(sku, quantity, addLine, removeLine),
                table
        );
        box.setPrefWidth(720);

        Node save = dialog.getDialogPane().lookupButton(saveButtonType());
        save.addEventFilter(ActionEvent.ACTION, event -> {
            if (orderDate.getValue() == null || rows.isEmpty()) {
                alert(Alert.AlertType.WARNING, "Thiếu dữ liệu", "Vui lòng nhập ngày đặt và ít nhất một sản phẩm.");
                event.consume();
            }
        });

        dialog.getDialogPane().setContent(box);
        dialog.setResultConverter(type -> type == saveButtonType()
                ? new OrderRequestUpsertRequest(
                orderDate.getValue(),
                notes.getText().isBlank() ? null : notes.getText().trim(),
                rows.stream()
                        .map(row -> new OrderRequestItemRequest(row.sku().id(), row.quantity(), null))
                        .toList())
                : null);
        return dialog.showAndWait();
    }

    private void showOrderRequestDetail(OrderRequestResponse request, String sentToSites) {
        TableView<OrderRequestItemResponse> items = new TableView<>();
        items.getColumns().setAll(List.of(
                column("SKU", 130, OrderRequestItemResponse::skuCode),
                column("Tên", 220, OrderRequestItemResponse::skuName),
                column("SL", 100, OrderRequestItemResponse::quantity),
                column("ĐVT", 100, OrderRequestItemResponse::unit)
        ));
        items.getItems().setAll(request.items() == null ? List.of() : request.items());
        items.setPrefHeight(240);
        showContentDialog("Request " + request.code(), detailHeader(fields(
                "Trạng thái", request.status(),
                "Gửi đến", sentToSites,
                "Ngày đặt", request.expectedDate(),
                "Tạo bởi", request.createdByName(),
                "Xử lý bởi", request.processedByName(),
                "Ghi chú", request.notes(),
                "Lý do hủy", request.cancelReason()
        ), items));
    }

    private void showPurchaseOrders() {
        VBox page = page("Đơn đặt hàng (PO)");

        ComboBox<String> statusFilter = enumFilter(POStatus.class, "Tất cả trạng thái");
        TextField search = new TextField();
        search.setPromptText("Tìm mã PO, mã yêu cầu, cơ sở");
        Button refresh = secondaryButton("Làm mới");
        Button detail = secondaryButton("Chi tiết");
        Button updateStatus = secondaryButton("Cập nhật trạng thái");
        Button confirmPo = primaryButton("Xác nhận đơn hàng");
        Button cancel = secondaryButton("Hủy");

        TableView<PurchaseOrderResponse> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.getColumns().setAll(List.of(
                column("Mã", 140, PurchaseOrderResponse::code),
                column("Trạng thái", 130, PurchaseOrderResponse::status),
                column("Yêu cầu", 140, PurchaseOrderResponse::requestCode),
                column("Cơ sở", 180, row -> row.siteCode() + " - " + row.siteName()),
                column("Vận chuyển", 90, PurchaseOrderResponse::transportMethod),
                column("Dự kiến", 110, PurchaseOrderResponse::expectedArrivalDate),
                column("Thực tế", 110, PurchaseOrderResponse::actualArrivalDate),
                column("Số lượng SP", 110, PurchaseOrderResponse::itemCount)
        ));
        VBox.setVgrow(table, Priority.ALWAYS);

        Runnable load = () -> call("Đang tải PO...", database.listPurchaseOrders(
                enumSelection(statusFilter, POStatus.class, "Tất cả trạng thái"), 
                currentUser.role() == Role.SITE ? currentUser.siteId() : null, 
                search.getText()), table.getItems()::setAll);
        refresh.setOnAction(event -> load.run());
        search.setOnAction(event -> load.run());
        detail.setOnAction(event -> withSelected(table, row -> call("Đang tải chi tiết PO...", database.purchaseOrderDetail(row.id()), this::showPurchaseOrderDetail)));
        updateStatus.setOnAction(event -> withSelected(table, row -> showPoStatusDialog(row).ifPresent(change -> call("Đang cập nhật trạng thái PO...", database.updatePurchaseOrderStatus(row.id(), change.status(), change.actualArrivalDate()), updated -> load.run()))));
        confirmPo.setOnAction(event -> withSelected(table, row -> {
            if (row.status() != POStatus.PENDING_CONFIRM) {
                alert(Alert.AlertType.WARNING, "Không thể xác nhận", "Chỉ có thể xác nhận đơn hàng đang chờ xác nhận (PENDING_CONFIRM).");
                return;
            }
            call("Đang xác nhận đơn hàng...", database.updatePurchaseOrderStatus(row.id(), POStatus.PREPARING, null), updated -> load.run());
        }));
        cancel.setOnAction(event -> withReason("Hủy PO", reason -> withSelected(table, row -> call("Đang hủy PO...", database.cancelPurchaseOrder(row.id(), reason), updated -> load.run()))));

        List<Node> tbNodes = new java.util.ArrayList<>();
        tbNodes.add(statusFilter);
        tbNodes.add(search);
        tbNodes.add(refresh);
        tbNodes.add(detail);
        if (currentUser.role() == Role.SITE) {
            tbNodes.add(confirmPo);
        } else {
            tbNodes.add(updateStatus);
            tbNodes.add(cancel);
        }

        page.getChildren().addAll(toolbar(tbNodes.toArray(new Node[0])), table);
        setPage(page);
        load.run();
    }

    private Optional<PoStatusDraft> showPoStatusDialog(PurchaseOrderResponse order) {
        Dialog<PoStatusDraft> dialog = new Dialog<>();
        dialog.setTitle("Cập nhật " + order.code());
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType(), ButtonType.CANCEL);

        ComboBox<POStatus> status = enumCombo(POStatus.class);
        status.setValue(order.status());
        DatePicker actualArrival = new DatePicker(order.actualArrivalDate());

        GridPane form = formGrid();
        form.addRow(0, new Label("Trạng thái"), status);
        form.addRow(1, new Label("Ngày đến thực tế"), actualArrival);

        dialog.getDialogPane().setContent(form);
        dialog.setResultConverter(type -> type == saveButtonType() ? new PoStatusDraft(status.getValue(), actualArrival.getValue()) : null);
        return dialog.showAndWait();
    }

    private void showPurchaseOrderDetail(PurchaseOrderResponse order) {
        TableView<PurchaseOrderItemResponse> items = poItemsTable();
        items.getItems().setAll(order.items() == null ? List.of() : order.items());
        items.setPrefHeight(260);
        showContentDialog("Purchase order " + order.code(), detailHeader(fields(
                "Trạng thái", order.status(),
                "Yêu cầu", order.requestCode(),
                "Cơ sở", order.siteCode() + " - " + order.siteName(),
                "Vận chuyển", order.transportMethod(),
                "Ngày đến dự kiến", order.expectedArrivalDate(),
                "Ngày đến thực tế", order.actualArrivalDate(),
                "Lý do hủy", order.cancelReason()
        ), items));
    }

    private void showMyInventory() {
        String siteName = currentUser.siteName() != null ? currentUser.siteName() : "Cơ sở của tôi";
        VBox page = page("Tồn kho của tôi - " + siteName);

        Button refresh = secondaryButton("Làm mới");
        Button upsertInventory = primaryButton("Cập nhật số lượng");

        TableView<InventoryResponse> inventoryTable = new TableView<>();
        inventoryTable.getColumns().setAll(List.of(
                column("SKU", 130, InventoryResponse::skuCode),
                column("Tên", 250, InventoryResponse::skuName),
                column("SL", 100, InventoryResponse::quantity),
                column("ĐVT", 100, InventoryResponse::unit),
                column("Cập nhật gần nhất", 160, InventoryResponse::updatedAt)
        ));
        VBox.setVgrow(inventoryTable, Priority.ALWAYS);

        ObservableList<SkuResponse> skuRows = FXCollections.observableArrayList();
        Runnable loadSkus = () -> call("Đang tải danh sách SKU...", database.listSkus(""), skuRows::setAll);
        Runnable loadInventory = () -> {
            if (currentUser.siteId() == null) return;
            call("Đang tải tồn kho...", database.siteInventory(currentUser.siteId()), inventoryTable.getItems()::setAll);
        };

        refresh.setOnAction(event -> {
            loadSkus.run();
            loadInventory.run();
        });

        upsertInventory.setOnAction(event -> {
            if (currentUser.siteId() == null) {
                alert(Alert.AlertType.WARNING, "Lỗi", "Tài khoản của bạn chưa được liên kết với cơ sở nào.");
                return;
            }
            showInventoryDialog(skuRows).ifPresent(draft -> call("Đang lưu tồn kho...", 
                database.upsertInventory(currentUser.siteId(), draft.sku().id(), draft.quantity()), 
                saved -> loadInventory.run()));
        });

        page.getChildren().addAll(toolbar(refresh, upsertInventory), inventoryTable);
        setPage(page);
        loadSkus.run();
        loadInventory.run();
    }

    private void showSites() {
        VBox page = page("Cơ sở & Tồn kho");

        TextField siteSearch = new TextField();
        siteSearch.setPromptText("Tìm cơ sở");
        Button refreshSites = secondaryButton("Làm mới");
        Button newSite = primaryButton("Cơ sở mới");
        Button editSite = secondaryButton("Sửa cơ sở");
        Button newSku = primaryButton("SKU mới");
        Button editSku = secondaryButton("Sửa SKU");
        Button upsertInventory = secondaryButton("Cập nhật tồn kho");

        TableView<SiteResponse> siteTable = new TableView<>();
        siteTable.getColumns().setAll(List.of(
                column("Mã", 90, SiteResponse::code),
                column("Tên", 170, SiteResponse::name),
                column("Quốc gia", 120, SiteResponse::country),
                column("Đường biển", 100, SiteResponse::seaLeadTime),
                column("Đường hàng không", 100, SiteResponse::airLeadTime),
                column("Hoạt động", 100, SiteResponse::active),
                column("SKU", 100, SiteResponse::skuCount)
        ));

        TableView<InventoryResponse> inventoryTable = new TableView<>();
        inventoryTable.getColumns().setAll(List.of(
                column("SKU", 110, InventoryResponse::skuCode),
                column("Tên", 180, InventoryResponse::skuName),
                column("SL", 100, InventoryResponse::quantity),
                column("ĐVT", 100, InventoryResponse::unit),
                column("Cập nhật", 140, InventoryResponse::updatedAt)
        ));

        ObservableList<SkuResponse> skuRows = FXCollections.observableArrayList();
        TableView<SkuResponse> skuTable = new TableView<>(skuRows);
        skuTable.getColumns().setAll(List.of(
                column("Mã", 110, SkuResponse::code),
                column("Tên", 200, SkuResponse::name),
                column("ĐVT", 100, SkuResponse::unit),
                column("Mô tả", 240, SkuResponse::description)
        ));

        Runnable loadSites = () -> call("Đang tải cơ sở...", database.listSites(null, siteSearch.getText()), siteTable.getItems()::setAll);
        Runnable loadSkus = () -> call("Đang tải danh sách SKU...", database.listSkus(""), skuRows::setAll);
        Consumer<SiteResponse> loadInventory = site -> {
            if (site == null) {
                inventoryTable.getItems().clear();
                return;
            }
            call("Đang tải tồn kho...", database.siteInventory(site.id()), inventoryTable.getItems()::setAll);
        };

        siteTable.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> loadInventory.accept(selected));
        refreshSites.setOnAction(event -> {
            loadSites.run();
            loadSkus.run();
        });
        siteSearch.setOnAction(event -> loadSites.run());
        newSite.setOnAction(event -> showSiteDialog(null).ifPresent(request -> call("Đang tạo cơ sở...", database.createSite(request), saved -> loadSites.run())));
        editSite.setOnAction(event -> withSelected(siteTable, site -> showSiteDialog(site).ifPresent(request -> call("Đang cập nhật cơ sở...", database.updateSite(site.id(), request), saved -> loadSites.run()))));
        newSku.setOnAction(event -> showSkuDialog(null).ifPresent(request -> call("Đang tạo SKU...", database.createSku(request), saved -> loadSkus.run())));
        editSku.setOnAction(event -> withSelected(skuTable, sku -> showSkuDialog(sku).ifPresent(request -> call("Đang cập nhật SKU...", database.updateSku(sku.id(), request), saved -> loadSkus.run()))));
        upsertInventory.setOnAction(event -> withSelected(siteTable, site -> showInventoryDialog(skuRows).ifPresent(draft -> call("Đang lưu tồn kho...", database.upsertInventory(site.id(), draft.sku().id(), draft.quantity()), saved -> loadInventory.accept(site)))));

        List<Node> leftTbNodes = new java.util.ArrayList<>();
        leftTbNodes.add(siteSearch);
        leftTbNodes.add(refreshSites);
        List<Node> rightTbNodes = new java.util.ArrayList<>();
        List<Node> bottomTbNodes = new java.util.ArrayList<>();
        
        if (currentUser.role() == Role.ADMIN) {
            leftTbNodes.add(newSite);
            leftTbNodes.add(editSite);
            rightTbNodes.add(upsertInventory);
            bottomTbNodes.add(newSku);
            bottomTbNodes.add(editSku);
        }

        VBox left = new VBox(10, toolbar(leftTbNodes.toArray(new Node[0])), siteTable);
        VBox right = rightTbNodes.isEmpty() ? new VBox(10, inventoryTable) : new VBox(10, toolbar(rightTbNodes.toArray(new Node[0])), inventoryTable);
        VBox bottom = bottomTbNodes.isEmpty() ? new VBox(10, skuTable) : new VBox(10, toolbar(bottomTbNodes.toArray(new Node[0])), skuTable);
        VBox.setVgrow(siteTable, Priority.ALWAYS);
        VBox.setVgrow(inventoryTable, Priority.ALWAYS);
        VBox.setVgrow(skuTable, Priority.ALWAYS);

        SplitPane split = new SplitPane(left, right);
        split.setDividerPositions(0.55);
        VBox.setVgrow(split, Priority.ALWAYS);

        TabPane tabs = new TabPane();
        Tab sitesTab = new Tab("Cơ sở và tồn kho", split);
        sitesTab.setClosable(false);
        Tab skusTab = new Tab("SKU", bottom);
        skusTab.setClosable(false);
        tabs.getTabs().addAll(sitesTab, skusTab);
        VBox.setVgrow(tabs, Priority.ALWAYS);

        page.getChildren().add(tabs);
        setPage(page);
        loadSites.run();
        loadSkus.run();
    }

    private Optional<SiteRequest> showSiteDialog(SiteResponse site) {
        Dialog<SiteRequest> dialog = new Dialog<>();
        dialog.setTitle(site == null ? "Cơ sở mới" : "Sửa cơ sở");
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType(), ButtonType.CANCEL);

        TextField code = new TextField(site == null ? "" : site.code());
        TextField name = new TextField(site == null ? "" : site.name());
        TextField country = new TextField(site == null ? "" : site.country());
        Spinner<Integer> sea = integerSpinner(0, site == null ? 30 : site.seaLeadTime(), 365);
        Spinner<Integer> air = integerSpinner(0, site == null ? 7 : site.airLeadTime(), 365);
        CheckBox active = new CheckBox("Hoạt động");
        active.setSelected(site == null || site.active());

        GridPane form = formGrid();
        form.addRow(0, new Label("Mã"), code);
        form.addRow(1, new Label("Tên"), name);
        form.addRow(2, new Label("Quốc gia"), country);
        form.addRow(3, new Label("Thời gian biển (ngày)"), sea);
        form.addRow(4, new Label("Thời gian bay (ngày)"), air);
        form.addRow(5, new Label("Trạng thái"), active);

        Node save = dialog.getDialogPane().lookupButton(saveButtonType());
        save.addEventFilter(ActionEvent.ACTION, event -> {
            if (code.getText().isBlank() || name.getText().isBlank() || country.getText().isBlank()) {
                alert(Alert.AlertType.WARNING, "Thiếu dữ liệu", "Mã, tên và quốc gia là bắt buộc.");
                event.consume();
            }
        });
        dialog.getDialogPane().setContent(form);
        dialog.setResultConverter(type -> type == saveButtonType()
                ? new SiteRequest(code.getText().trim(), name.getText().trim(), country.getText().trim(), sea.getValue(), air.getValue(), active.isSelected())
                : null);
        return dialog.showAndWait();
    }

    private Optional<SkuRequest> showSkuDialog(SkuResponse sku) {
        Dialog<SkuRequest> dialog = new Dialog<>();
        dialog.setTitle(sku == null ? "SKU mới" : "Sửa SKU");
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType(), ButtonType.CANCEL);

        TextField code = new TextField(sku == null ? "" : sku.code());
        TextField name = new TextField(sku == null ? "" : sku.name());
        TextField unit = new TextField(sku == null ? "" : sku.unit());
        TextArea description = new TextArea(sku == null ? "" : safe(sku.description()));
        description.setPrefRowCount(3);

        GridPane form = formGrid();
        form.addRow(0, new Label("Mã"), code);
        form.addRow(1, new Label("Tên"), name);
        form.addRow(2, new Label("ĐVT"), unit);
        form.addRow(3, new Label("Mô tả"), description);

        Node save = dialog.getDialogPane().lookupButton(saveButtonType());
        save.addEventFilter(ActionEvent.ACTION, event -> {
            if (code.getText().isBlank() || name.getText().isBlank() || unit.getText().isBlank()) {
                alert(Alert.AlertType.WARNING, "Thiếu dữ liệu", "Mã, tên và ĐVT là bắt buộc.");
                event.consume();
            }
        });
        dialog.getDialogPane().setContent(form);
        dialog.setResultConverter(type -> type == saveButtonType()
                ? new SkuRequest(code.getText().trim(), name.getText().trim(), unit.getText().trim(), description.getText().isBlank() ? null : description.getText().trim())
                : null);
        return dialog.showAndWait();
    }

    private Optional<InventoryDraft> showInventoryDialog(List<SkuResponse> skus) {
        Dialog<InventoryDraft> dialog = new Dialog<>();
        dialog.setTitle("Cập nhật tồn kho");
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType(), ButtonType.CANCEL);

        ComboBox<SkuResponse> sku = skuCombo(skus);
        Spinner<Integer> quantity = integerSpinner(0, 0, 1_000_000);

        GridPane form = formGrid();
        form.addRow(0, new Label("SKU"), sku);
        form.addRow(1, new Label("Số lượng"), quantity);

        Node save = dialog.getDialogPane().lookupButton(saveButtonType());
        save.addEventFilter(ActionEvent.ACTION, event -> {
            if (sku.getValue() == null) {
                alert(Alert.AlertType.WARNING, "Chưa chọn SKU", "Vui lòng chọn một SKU.");
                event.consume();
            }
        });
        dialog.getDialogPane().setContent(form);
        dialog.setResultConverter(type -> type == saveButtonType() ? new InventoryDraft(sku.getValue(), quantity.getValue()) : null);
        return dialog.showAndWait();
    }

    private void showShipments() {
        VBox page = page("Theo dõi vận chuyển");

        Button refresh = secondaryButton("Làm mới");
        Button addTracking = primaryButton("Thêm theo dõi");
        TableView<PurchaseOrderResponse> orders = new TableView<>();
        orders.getColumns().setAll(List.of(
                column("PO", 140, PurchaseOrderResponse::code),
                column("Trạng thái", 130, PurchaseOrderResponse::status),
                column("Cơ sở", 180, row -> row.siteCode() + " - " + row.siteName()),
                column("Vận chuyển", 90, PurchaseOrderResponse::transportMethod),
                column("Dự kiến", 110, PurchaseOrderResponse::expectedArrivalDate)
        ));

        TableView<TrackingResponse> history = new TableView<>();
        history.getColumns().setAll(List.of(
                column("Thời gian", 150, TrackingResponse::timestamp),
                column("Trạng thái", 130, TrackingResponse::status),
                column("Vị trí", 170, TrackingResponse::location),
                column("Cập nhật bởi", 160, TrackingResponse::updatedByName),
                column("Ghi chú", 260, TrackingResponse::notes)
        ));

        Consumer<PurchaseOrderResponse> loadHistory = order -> {
            if (order == null) {
                history.getItems().clear();
                return;
            }
            call("Đang tải lịch sử theo dõi...", database.trackingHistory(order.id()), history.getItems()::setAll);
        };
        orders.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> loadHistory.accept(selected));

        Runnable load = () -> call("Đang tải vận chuyển...", database.inTransitShipments(), orders.getItems()::setAll);
        refresh.setOnAction(event -> load.run());
        addTracking.setOnAction(event -> withSelected(orders, order -> showTrackingDialog(order).ifPresent(request -> call("Đang thêm theo dõi...", database.addTracking(order.id(), request), saved -> {
            load.run();
            loadHistory.accept(order);
        }))));

        VBox left = new VBox(10, toolbar(refresh, addTracking), orders);
        VBox right = new VBox(10, new Label("Lịch sử theo dõi"), history);
        VBox.setVgrow(orders, Priority.ALWAYS);
        VBox.setVgrow(history, Priority.ALWAYS);
        SplitPane split = new SplitPane(left, right);
        split.setDividerPositions(0.55);
        VBox.setVgrow(split, Priority.ALWAYS);

        page.getChildren().add(split);
        setPage(page);
        load.run();
    }

    private Optional<TrackingRequest> showTrackingDialog(PurchaseOrderResponse order) {
        Dialog<TrackingRequest> dialog = new Dialog<>();
        dialog.setTitle("Thêm theo dõi cho " + order.code());
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType(), ButtonType.CANCEL);

        ComboBox<POStatus> status = enumCombo(POStatus.class);
        status.setValue(order.status() == POStatus.PREPARING ? POStatus.SHIPPING : order.status());
        TextField location = new TextField();
        TextArea notes = new TextArea();
        notes.setPrefRowCount(3);
        TextField evidence = new TextField();

        GridPane form = formGrid();
        form.addRow(0, new Label("Trạng thái"), status);
        form.addRow(1, new Label("Vị trí"), location);
        form.addRow(2, new Label("Ghi chú"), notes);
        form.addRow(3, new Label("URL minh chứng"), evidence);

        dialog.getDialogPane().setContent(form);
        dialog.setResultConverter(type -> type == saveButtonType()
                ? new TrackingRequest(status.getValue(), blankToNull(location.getText()), blankToNull(notes.getText()), blankToNull(evidence.getText()))
                : null);
        return dialog.showAndWait();
    }

    private void showProfile() {
        VBox page = page("Hồ sơ cá nhân");

        TextField fullName = new TextField(currentUser == null ? "" : currentUser.fullName());
        TextField employeeId = new TextField(currentUser == null ? "" : currentUser.employeeId());
        TextField email = new TextField(currentUser == null ? "" : currentUser.email());
        email.setDisable(true);
        TextField role = new TextField(currentUser == null ? "" : currentUser.role().name());
        role.setDisable(true);
        Button saveProfile = primaryButton("Lưu hồ sơ");

        GridPane profileForm = formGrid();
        profileForm.addRow(0, new Label("Họ và tên"), fullName);
        profileForm.addRow(1, new Label("Mã nhân viên"), employeeId);
        profileForm.addRow(2, new Label("Email"), email);
        profileForm.addRow(3, new Label("Vai trò"), role);
        profileForm.add(saveProfile, 1, 4);

        PasswordField oldPassword = new PasswordField();
        PasswordField newPassword = new PasswordField();
        Button changePassword = secondaryButton("Đổi mật khẩu");
        GridPane passwordForm = formGrid();
        passwordForm.addRow(0, new Label("Mật khẩu hiện tại"), oldPassword);
        passwordForm.addRow(1, new Label("Mật khẩu mới"), newPassword);
        passwordForm.add(changePassword, 1, 2);

        saveProfile.setOnAction(event -> call("Đang lưu hồ sơ...", database.updateProfile(fullName.getText(), employeeId.getText()), result -> call("Đang tải lại hồ sơ...", database.profile(), user -> {
            currentUser = user;
            alert(Alert.AlertType.INFORMATION, "Đã lưu hồ sơ", "Thông tin hồ sơ đã được cập nhật.");
            showWorkspace();
            showProfile();
        })));
        changePassword.setOnAction(event -> call("Đang đổi mật khẩu...", database.changePassword(oldPassword.getText(), newPassword.getText()), result -> {
            oldPassword.clear();
            newPassword.clear();
            alert(Alert.AlertType.INFORMATION, "Đã đổi mật khẩu", "Mật khẩu đã được cập nhật.");
        }));

        TabPane tabs = new TabPane();
        Tab accountTab = new Tab("Tài khoản", new VBox(18, section("Hồ sơ cá nhân", profileForm), section("Mật khẩu", passwordForm)));
        accountTab.setClosable(false);
        tabs.getTabs().add(accountTab);

        // adminUsersTab removed and extracted to showUsers()

        VBox.setVgrow(tabs, Priority.ALWAYS);
        page.getChildren().add(tabs);
        setPage(page);
    }

    private void showUsers() {
        VBox page = page("Quản lý tài khoản");

        ComboBox<String> statusFilter = enumFilter(AccountStatus.class, "Tất cả trạng thái");
        TextField search = new TextField();
        search.setPromptText("Tìm người dùng");
        Button refresh = secondaryButton("Làm mới");
        Button approve = secondaryButton("Phê duyệt");
        Button block = secondaryButton("Khóa");
        Button roleBtn = secondaryButton("Đổi vai trò");

        TableView<UserResponse> users = new TableView<>();
        users.getColumns().setAll(List.of(
                column("Tên", 170, UserResponse::fullName),
                column("Email", 220, UserResponse::email),
                column("Nhân viên", 120, UserResponse::employeeId),
                column("Vai trò", 140, UserResponse::role),
                column("Trạng thái", 110, UserResponse::status),
                column("Ngày tạo", 150, UserResponse::createdAt)
        ));
        VBox.setVgrow(users, Priority.ALWAYS);

        Runnable load = () -> call("Đang tải người dùng...", database.listUsers(enumSelection(statusFilter, AccountStatus.class, "Tất cả trạng thái"), search.getText()), users.getItems()::setAll);
        refresh.setOnAction(event -> load.run());
        search.setOnAction(event -> load.run());
        approve.setOnAction(event -> withSelected(users, user -> call("Đang phê duyệt...", database.approveUser(user.id()), result -> load.run())));
        block.setOnAction(event -> withSelected(users, user -> call("Đang khóa...", database.blockUser(user.id()), result -> load.run())));
        roleBtn.setOnAction(event -> withSelected(users, user -> showRoleDialog(user).ifPresent(newRole -> call("Đang cập nhật vai trò...", database.updateRole(user.id(), newRole), result -> load.run()))));

        page.getChildren().addAll(toolbar(statusFilter, search, refresh, approve, block, roleBtn), users);
        setPage(page);
        load.run();
    }

    private Optional<Role> showRoleDialog(UserResponse user) {
        Dialog<Role> dialog = new Dialog<>();
        dialog.setTitle("Đổi vai trò cho " + user.fullName());
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType(), ButtonType.CANCEL);

        ComboBox<Role> role = enumCombo(Role.class);
        role.setValue(user.role());
        dialog.getDialogPane().setContent(labeled("Vai trò", role));
        dialog.setResultConverter(type -> type == saveButtonType() ? role.getValue() : null);
        return dialog.showAndWait();
    }

    private TableView<PurchaseOrderItemResponse> poItemsTable() {
        TableView<PurchaseOrderItemResponse> items = new TableView<>();
        items.getColumns().setAll(List.of(
                column("SKU", 120, PurchaseOrderItemResponse::skuCode),
                column("Tên", 220, PurchaseOrderItemResponse::skuName),
                column("SL đặt", 110, PurchaseOrderItemResponse::quantityOrdered),
                column("SL nhận", 110, PurchaseOrderItemResponse::quantityReceived),
                column("Chênh lệch", 100, PurchaseOrderItemResponse::difference),
                column("Ghi chú", 220, PurchaseOrderItemResponse::notes)
        ));
        return items;
    }

    private <T> TableColumn<T, String> column(String title, int width, Function<T, Object> extractor) {
        TableColumn<T, String> column = new TableColumn<>(title);
        column.setPrefWidth(width);
        column.setCellValueFactory(data -> new ReadOnlyStringWrapper(format(extractor.apply(data.getValue()))));
        return column;
    }

    private VBox page(String title) {
        Label label = new Label(title);
        label.getStyleClass().add("page-title");
        VBox page = new VBox(16, label);
        page.setPadding(new Insets(24));
        page.getStyleClass().add("page");
        return page;
    }

    private void setPage(Node node) {
        content.getChildren().setAll(node);
    }

    private Node metric(String label, long value) {
        VBox box = new VBox(8);
        box.getStyleClass().add("metric");
        Label number = new Label(Long.toString(value));
        number.getStyleClass().add("metric-number");
        Label text = new Label(label);
        text.getStyleClass().add("muted");
        box.getChildren().addAll(number, text);
        return box;
    }

    private Button navButton(String text, Runnable action) {
        Button button = new Button(text);
        button.getStyleClass().add("nav-button");
        button.setMaxWidth(Double.MAX_VALUE);
        button.setOnAction(event -> action.run());
        return button;
    }

    private Button primaryButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("primary-button");
        return button;
    }

    private Button secondaryButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("secondary-button");
        return button;
    }

    private ButtonType saveButtonType() {
        return SAVE_BUTTON;
    }

    private GridPane formGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        ColumnConstraints label = new ColumnConstraints();
        label.setMinWidth(120);
        ColumnConstraints field = new ColumnConstraints();
        field.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(label, field);
        return grid;
    }

    private FlowPane toolbar(Node... nodes) {
        FlowPane toolbar = new FlowPane(10, 10, nodes);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.getStyleClass().add("toolbar");
        for (Node node : nodes) {
            if (node instanceof TextField textField) {
                textField.setPrefWidth(220);
            }
        }
        return toolbar;
    }

    private Node labeled(String label, Node field) {
        VBox box = new VBox(6, new Label(label), field);
        box.getStyleClass().add("field-block");
        return box;
    }

    private Node section(String title, Node content) {
        VBox box = new VBox(12, new Label(title), content);
        box.getStyleClass().add("section");
        return box;
    }

    private Region spacer() {
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        return spacer;
    }

    private Spinner<Integer> integerSpinner(int min, int value, int max) {
        Spinner<Integer> spinner = new Spinner<>();
        spinner.setEditable(true);
        spinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(min, max, value));
        spinner.setPrefWidth(110);
        return spinner;
    }

    private <E extends Enum<E>> ComboBox<E> enumCombo(Class<E> type) {
        ComboBox<E> combo = new ComboBox<>(FXCollections.observableArrayList(type.getEnumConstants()));
        combo.setConverter(new StringConverter<>() {
            @Override
            public String toString(E value) {
                return value == null ? "" : value.name();
            }

            @Override
            public E fromString(String value) {
                return value == null || value.isBlank() ? null : Enum.valueOf(type, value);
            }
        });
        combo.setPrefWidth(190);
        return combo;
    }

    private <E extends Enum<E>> ComboBox<String> enumFilter(Class<E> type, String allLabel) {
        ComboBox<String> combo = new ComboBox<>();
        combo.getItems().add(allLabel);
        Arrays.stream(type.getEnumConstants()).map(Enum::name).forEach(combo.getItems()::add);
        combo.setValue(allLabel);
        combo.setPrefWidth(170);
        return combo;
    }

    private <E extends Enum<E>> E enumSelection(ComboBox<String> combo, Class<E> type, String allLabel) {
        String value = combo.getValue();
        return value == null || value.equals(allLabel) ? null : Enum.valueOf(type, value);
    }

    private ComboBox<SkuResponse> skuCombo(List<SkuResponse> skus) {
        ComboBox<SkuResponse> combo = new ComboBox<>(FXCollections.observableArrayList(skus));
        combo.setConverter(new StringConverter<>() {
            @Override
            public String toString(SkuResponse value) {
                return value == null ? "" : value.code() + " - " + value.name();
            }

            @Override
            public SkuResponse fromString(String value) {
                return skus.stream()
                        .filter(sku -> (sku.code() + " - " + sku.name()).equals(value))
                        .findFirst()
                        .orElse(null);
            }
        });
        combo.setPrefWidth(260);
        return combo;
    }

    private Node detailHeader(Map<String, Object> fields, Node detail) {
        GridPane grid = formGrid();
        int row = 0;
        for (Map.Entry<String, Object> entry : fields.entrySet()) {
            grid.addRow(row++, new Label(entry.getKey()), new Label(format(entry.getValue())));
        }
        return new VBox(14, grid, new Separator(), detail);
    }

    private Map<String, Object> fields(Object... pairs) {
        Map<String, Object> values = new LinkedHashMap<>();
        for (int i = 0; i + 1 < pairs.length; i += 2) {
            values.put(String.valueOf(pairs[i]), pairs[i + 1]);
        }
        return values;
    }

    private void showContentDialog(String title, Node contentNode) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        ScrollPane scroll = new ScrollPane(contentNode);
        scroll.setFitToWidth(true);
        scroll.setPrefSize(820, 520);
        dialog.getDialogPane().setContent(scroll);
        dialog.showAndWait();
    }

    private <T> void withSelected(TableView<T> table, Consumer<T> action) {
        T selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            alert(Alert.AlertType.WARNING, "Chưa chọn dòng nào", "Vui lòng chọn một dòng trước.");
            return;
        }
        action.accept(selected);
    }

    private void withReason(String title, Consumer<String> action) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(title);
        dialog.setHeaderText(title);
        dialog.setContentText("Lý do");
        dialog.showAndWait()
                .map(String::trim)
                .filter(reason -> !reason.isBlank())
                .ifPresent(action);
    }

    private <T> void call(String message, CompletableFuture<T> future, Consumer<T> onSuccess) {
        setStatus(message);
        future.whenComplete((value, error) -> Platform.runLater(() -> {
            if (error != null) {
                handleFailure(error);
                return;
            }
            onSuccess.accept(value);
            setStatus("Sẵn sàng");
        }));
    }

    private void handleFailure(Throwable error) {
        Throwable cause = rootCause(error);
        String message = errorMessage(cause);
        setStatus(message);
        if (cause instanceof DatabaseException databaseException && databaseException.statusCode() == 401) {
            preferences.remove(PREF_SESSION_USER_ID);
            alert(Alert.AlertType.WARNING, "Phiên đăng nhập hết hạn", "Vui lòng đăng nhập lại.");
            showLogin();
            return;
        }
        alert(Alert.AlertType.ERROR, "Yêu cầu thất bại", message);
    }

    private Throwable rootCause(Throwable error) {
        Throwable current = error;
        while ((current instanceof CompletionException || current instanceof ExecutionException) && current.getCause() != null) {
            current = current.getCause();
        }
        return current;
    }

    private String errorMessage(Throwable error) {
        Throwable cause = rootCause(error);
        return cause.getMessage() == null ? cause.toString() : cause.getMessage();
    }

    private void setStatus(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);
        }
    }

    private void alert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private String format(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof Instant instant) {
            return DATE_TIME.format(instant.atZone(ZoneId.systemDefault()));
        }
        if (value instanceof LocalDate localDate) {
            return localDate.toString();
        }
        return value.toString();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private record OrderLineDraft(SkuResponse sku, int quantity) {
    }

    private record PoStatusDraft(POStatus status, LocalDate actualArrivalDate) {
    }

    private record InventoryDraft(SkuResponse sku, int quantity) {
    }

}



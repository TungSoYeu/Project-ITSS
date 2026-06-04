package com.ooas.desktop.warehouse.ui.component;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public final class WarehouseUi {
    private WarehouseUi() {
    }

    public static VBox page(String title) {
        Label label = new Label(title);
        label.getStyleClass().add("warehouse-page-title");
        VBox page = new VBox(16, label);
        page.getStyleClass().add("warehouse-page");
        page.setPadding(new Insets(28));
        return page;
    }

    public static Node section(String title, Node content) {
        Label label = new Label(title);
        label.getStyleClass().add("warehouse-section-title");
        VBox section = new VBox(12, label, content);
        section.getStyleClass().add("warehouse-section");
        return section;
    }

    public static FlowPane toolbar(Node... nodes) {
        FlowPane bar = new FlowPane(10, 10, nodes);
        bar.getStyleClass().add("warehouse-toolbar");
        bar.setAlignment(Pos.CENTER_LEFT);
        return bar;
    }

    public static Node labeled(String label, Node field) {
        VBox block = new VBox(5, new Label(label), field);
        block.getStyleClass().add("warehouse-field");
        return block;
    }

    public static GridPane grid() {
        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(11);
        return grid;
    }

    public static void addInfo(GridPane grid, int row, int column, String label, Object value) {
        Label key = new Label(label);
        key.getStyleClass().add("warehouse-info-key");
        Label text = new Label(format(value));
        text.setWrapText(true);
        grid.add(key, column, row);
        grid.add(text, column + 1, row);
    }

    public static Button primaryButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("warehouse-primary-button");
        return button;
    }

    public static Button secondaryButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("warehouse-secondary-button");
        return button;
    }

    public static Button navButton(String text, Runnable action) {
        Button button = new Button(text);
        button.getStyleClass().add("warehouse-nav-button");
        button.setMaxWidth(Double.MAX_VALUE);
        button.setOnAction(event -> action.run());
        return button;
    }

    public static Spinner<Integer> spinner(int value) {
        Spinner<Integer> spinner = new Spinner<>();
        spinner.setEditable(true);
        spinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 1_000_000, value));
        spinner.setPrefWidth(120);
        return spinner;
    }

    public static String format(Object value) {
        return value == null ? "" : value.toString();
    }

}



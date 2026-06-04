package com.ooas.desktop.warehouse.domain;

import com.ooas.desktop.shared.model.PurchaseOrderItemResponse;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

public record InspectionLine(
        PurchaseOrderItemResponse item,
        Spinner<Integer> quantity,
        ComboBox<String> condition,
        ComboBox<String> packaging,
        TextField inspectionNotes,
        Label difference,
        ComboBox<String> reason,
        ComboBox<String> resolution,
        TextField resolutionNotes,
        GridPane card
) {
}



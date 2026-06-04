package com.ooas.desktop.warehouse.ui.component;

import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;

public final class InspectionFields {
    private InspectionFields() {
    }

    public static ComboBox<String> condition() {
        return combo("Đạt", "Đạt", "Hư hỏng", "Móp/méo", "Trầy xước", "Ướt/ẩm", "Sai quy cách");
    }

    public static ComboBox<String> packaging() {
        return combo("Nguyên vẹn", "Nguyên vẹn", "Rách", "Móp/vỡ", "Mất niêm phong", "Sai nhãn");
    }

    public static ComboBox<String> reason() {
        return combo("Chưa chọn", "Chưa chọn", "Site giao thiếu", "Site giao thừa",
                "Hư hỏng trong vận chuyển", "Mất mát trong vận chuyển", "Sai sót khi đóng gói", "Lý do khác");
    }

    public static ComboBox<String> resolution() {
        return combo("Chưa chọn", "Chưa chọn", "Lập biên bản sai lệch", "Yêu cầu Site bổ sung",
                "Trả lại hàng thừa", "Chấp nhận số lượng thực tế");
    }

    private static ComboBox<String> combo(String value, String... values) {
        ComboBox<String> combo = new ComboBox<>(FXCollections.observableArrayList(values));
        combo.setValue(value);
        combo.setMaxWidth(Double.MAX_VALUE);
        return combo;
    }
}



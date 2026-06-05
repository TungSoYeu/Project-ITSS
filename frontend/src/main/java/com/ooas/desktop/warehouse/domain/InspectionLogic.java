package com.ooas.desktop.warehouse.domain;

import java.util.List;

public final class InspectionLogic {
    private InspectionLogic() {
    }

    public static int difference(InspectionLine line) {
        return line.quantity().getValue() - line.item().quantityOrdered();
    }

    public static boolean hasQualityIssue(InspectionLine line) {
        return !"Đạt".equals(line.condition().getValue()) || !"Nguyên vẹn".equals(line.packaging().getValue());
    }

    public static boolean hasIssue(InspectionLine line) {
        return difference(line) != 0 || hasQualityIssue(line);
    }

    public static void update(InspectionLine line) {
        int difference = difference(line);
        String quantity = difference == 0 ? "SL khớp"
                : difference < 0 ? "Thiếu " + Math.abs(difference) : "Thừa " + difference;
        String quality = hasQualityIssue(line) ? " · Không đạt chất lượng/bao bì" : "";
        line.difference().setText(quantity + quality);
        line.difference().getStyleClass().setAll(hasIssue(line) ? "difference-warning" : "difference-ok");
        if (!hasIssue(line)) {
            line.card().getStyleClass().remove("inspection-card-mismatch");
        } else if (!line.card().getStyleClass().contains("inspection-card-mismatch")) {
            line.card().getStyleClass().add("inspection-card-mismatch");
        }
    }

    public static void disableInspection(InspectionLine line, boolean disabled) {
        line.quantity().setDisable(disabled);
        line.condition().setDisable(disabled);
        line.packaging().setDisable(disabled);
        line.inspectionNotes().setDisable(disabled);
    }

    public static void disableResolution(InspectionLine line, boolean disabled) {
        line.reason().setDisable(disabled);
        line.resolution().setDisable(disabled);
        line.resolutionNotes().setDisable(disabled);
    }

    public static String summary(List<InspectionLine> lines) {
        int missing = lines.stream().mapToInt(line -> Math.max(0, -difference(line))).sum();
        int excess = lines.stream().mapToInt(line -> Math.max(0, difference(line))).sum();
        long quality = lines.stream().filter(InspectionLogic::hasQualityIssue).count();
        return missing == 0 && excess == 0 && quality == 0
                ? "Không còn sai lệch"
                : "Sai lệch hiện tại: thiếu " + missing + " · thừa " + excess + " · chất lượng/bao bì " + quality;
    }

    public static String note(InspectionLine line) {
        String inspection = "Kiểm kê: Tình trạng=" + line.condition().getValue()
                + "; Bao bì=" + line.packaging().getValue()
                + "; Số lượng đặt=" + line.item().quantityOrdered()
                + "; Số lượng thực nhận=" + line.quantity().getValue()
                + "; Chênh lệch=" + difference(line)
                + "; Ghi chú kiểm kê=" + line.inspectionNotes().getText().trim();
        return !hasIssue(line) ? inspection : inspection + "; Kết quả=Có chênh lệch/cần xử lý";
    }

    public static boolean unresolved(InspectionLine line) {
        return hasIssue(line) && ("Chưa chọn".equals(line.reason().getValue())
                || "Chưa chọn".equals(line.resolution().getValue())
                || line.resolutionNotes().getText().isBlank());
    }
}



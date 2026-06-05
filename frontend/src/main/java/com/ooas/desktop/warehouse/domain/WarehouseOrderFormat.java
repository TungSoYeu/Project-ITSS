package com.ooas.desktop.warehouse.domain;

import com.ooas.desktop.shared.model.POStatus;
import com.ooas.desktop.shared.model.PurchaseOrderResponse;

public final class WarehouseOrderFormat {
    private WarehouseOrderFormat() {
    }

    public static String inspectionStatus(PurchaseOrderResponse order) {
        return switch (order.status()) {
            case COMPLETED -> "Hoàn thành";
            case NEEDS_ACTION -> "Có chênh lệch/Cần xử lý";
            default -> "Chờ xác nhận nhập kho";
        };
    }

    public static String confirmationResult(PurchaseOrderResponse order) {
        if (order.items() == null || order.items().isEmpty()
                || (order.status() != POStatus.COMPLETED && order.status() != POStatus.NEEDS_ACTION)) {
            return "Chưa xác nhận";
        }
        int missing = order.items().stream().mapToInt(item -> Math.max(0, -item.difference())).sum();
        int excess = order.items().stream().mapToInt(item -> Math.max(0, item.difference())).sum();
        long quality = order.items().stream().filter(item -> hasQualityIssue(item.notes())).count();
        return missing == 0 && excess == 0 && quality == 0
                ? "Khớp thông tin"
                : "Thiếu " + missing + " · Thừa " + excess + " · CL/bao bì " + quality;
    }

    public static boolean visibleInWarehouse(PurchaseOrderResponse order) {
        return order.status() == POStatus.SHIPPING
                || order.status() == POStatus.ARRIVED
                || order.status() == POStatus.NEEDS_ACTION
                || order.status() == POStatus.COMPLETED;
    }

    private static boolean hasQualityIssue(String notes) {
        String value = notes == null ? "" : notes;
        boolean badCondition = value.contains("Tình trạng=") && !value.contains("Tình trạng=Đạt");
        boolean badPackaging = value.contains("Bao bì=") && !value.contains("Bao bì=Nguyên vẹn");
        return badCondition || badPackaging;
    }
}



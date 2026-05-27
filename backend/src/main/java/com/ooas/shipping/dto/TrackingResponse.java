package com.ooas.shipping.dto;

import com.ooas.domain.POStatus;
import com.ooas.domain.ShipmentTracking;
import java.time.Instant;

public record TrackingResponse(
        String id,
        String purchaseOrderId,
        POStatus status,
        String location,
        String notes,
        String evidenceFileUrl,
        String updatedById,
        String updatedByName,
        Instant timestamp
) {
    public static TrackingResponse from(ShipmentTracking tracking) {
        return new TrackingResponse(
                tracking.getId(),
                tracking.getPurchaseOrder().getId(),
                tracking.getStatus(),
                tracking.getLocation(),
                tracking.getNotes(),
                tracking.getEvidenceFileUrl(),
                tracking.getUpdatedBy().getId(),
                tracking.getUpdatedBy().getFullName(),
                tracking.getTimestamp()
        );
    }
}

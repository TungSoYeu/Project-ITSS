package com.ooas.desktop.shared.model;

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
}


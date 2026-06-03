package com.ooas.model;

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

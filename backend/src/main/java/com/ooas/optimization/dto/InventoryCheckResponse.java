package com.ooas.optimization.dto;

import java.util.List;

public record InventoryCheckResponse(
        String requestId,
        String requestCode,
        List<CandidateResponse> candidates,
        List<String> warnings
) {
}

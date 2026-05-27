package com.ooas.optimization.dto;

import java.util.List;

public record OptimizationResponse(
        String requestId,
        String requestCode,
        List<AllocationResponse> allocations,
        List<String> warnings
) {
}

package com.ooas.desktop.shared.model;

import java.util.List;

public record InventoryCheckResponse(String requestId, String requestCode, List<CandidateResponse> candidates, List<String> warnings) {
}


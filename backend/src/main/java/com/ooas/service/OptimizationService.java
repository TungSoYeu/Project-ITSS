package com.ooas.service;

import com.ooas.dto.OrderRequest;
import com.ooas.dto.InventoryCheckResponse;
import com.ooas.dto.OptimizationResponse;

public interface OptimizationService {
    InventoryCheckResponse checkInventory(String requestId);
    OptimizationResponse optimize(String requestId);
    OptimizationResponse optimize(OrderRequest request);
}

package com.ooas.controller;

import com.ooas.dto.InventoryCheckResponse;
import com.ooas.dto.OptimizationResponse;
import com.ooas.service.OptimizationService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/order-requests/{requestId}")
public class OptimizationController {

    private final OptimizationService optimizationService;

    public OptimizationController(OptimizationService optimizationService) {
        this.optimizationService = optimizationService;
    }

    @GetMapping("/inventory-check")
    @PreAuthorize("hasAnyAuthority('ADMIN','OVERSEAS_ORDER')")
    public InventoryCheckResponse checkInventory(@PathVariable String requestId) {
        return optimizationService.checkInventory(requestId);
    }

    @PostMapping("/optimize")
    @PreAuthorize("hasAnyAuthority('ADMIN','OVERSEAS_ORDER')")
    public OptimizationResponse optimize(@PathVariable String requestId) {
        return optimizationService.optimize(requestId);
    }
}

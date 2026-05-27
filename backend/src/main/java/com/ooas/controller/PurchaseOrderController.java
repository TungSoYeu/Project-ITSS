package com.ooas.controller;

import com.ooas.entity.POStatus;
import com.ooas.dto.CancelRequest;
import com.ooas.dto.PurchaseOrderResponse;
import com.ooas.dto.UpdatePoStatusRequest;
import com.ooas.service.PurchaseOrderService;
import com.ooas.security.JwtPrincipal;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    public PurchaseOrderController(PurchaseOrderService purchaseOrderService) {
        this.purchaseOrderService = purchaseOrderService;
    }

    @PostMapping("/order-requests/{requestId}/purchase-orders")
    @PreAuthorize("hasAnyAuthority('ADMIN','OVERSEAS_ORDER')")
    public List<PurchaseOrderResponse> generate(
            @PathVariable String requestId,
            @AuthenticationPrincipal JwtPrincipal principal
    ) {
        return purchaseOrderService.generateFromRequest(requestId, java.util.Objects.requireNonNull(principal.id()));
    }

    @GetMapping("/purchase-orders")
    public List<PurchaseOrderResponse> list(
            @RequestParam(required = false) POStatus status,
            @RequestParam(required = false) String siteId,
            @RequestParam(required = false) String search
    ) {
        return purchaseOrderService.list(status, siteId, search);
    }

    @GetMapping("/purchase-orders/{id}")
    public PurchaseOrderResponse detail(@PathVariable String id) {
        return purchaseOrderService.detail(id);
    }

    @PatchMapping("/purchase-orders/{id}/status")
    @PreAuthorize("hasAnyAuthority('ADMIN','OVERSEAS_ORDER','WAREHOUSE','SUPPLIER')")
    public PurchaseOrderResponse updateStatus(@PathVariable String id, @Valid @RequestBody UpdatePoStatusRequest request) {
        return purchaseOrderService.updateStatus(id, request);
    }

    @PatchMapping("/purchase-orders/{id}/cancel")
    @PreAuthorize("hasAnyAuthority('ADMIN','OVERSEAS_ORDER')")
    public PurchaseOrderResponse cancel(@PathVariable String id, @Valid @RequestBody CancelRequest request) {
        return purchaseOrderService.cancel(id, request);
    }
}

package com.ooas.controller;

import com.ooas.dto.PurchaseOrderResponse;
import com.ooas.security.JwtPrincipal;
import com.ooas.dto.TrackingRequest;
import com.ooas.dto.TrackingResponse;
import com.ooas.service.ShippingService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/shipments")
public class ShippingController {

    private final ShippingService shippingService;

    public ShippingController(ShippingService shippingService) {
        this.shippingService = shippingService;
    }

    @GetMapping("/in-transit")
    @PreAuthorize("hasAnyAuthority('ADMIN','OVERSEAS_ORDER','WAREHOUSE','SUPPLIER')")
    public List<PurchaseOrderResponse> inTransit() {
        return shippingService.inTransit();
    }

    @GetMapping("/purchase-orders/{id}/tracking")
    public List<TrackingResponse> history(@PathVariable String id) {
        return shippingService.history(id);
    }

    @PostMapping("/purchase-orders/{id}/tracking")
    @PreAuthorize("hasAnyAuthority('ADMIN','OVERSEAS_ORDER','SUPPLIER')")
    public TrackingResponse addTracking(
            @PathVariable String id,
            @Valid @RequestBody TrackingRequest request,
            @AuthenticationPrincipal JwtPrincipal principal
    ) {
        return shippingService.addTracking(id, request, java.util.Objects.requireNonNull(principal.id()));
    }
}

package com.ooas.warehouse;

import com.ooas.purchasing.dto.PurchaseOrderResponse;
import com.ooas.warehouse.dto.ReceivePurchaseOrderRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/warehouse")
public class WarehouseController {

    private final WarehouseService warehouseService;

    public WarehouseController(WarehouseService warehouseService) {
        this.warehouseService = warehouseService;
    }

    @GetMapping("/inbound")
    @PreAuthorize("hasAnyAuthority('ADMIN','WAREHOUSE')")
    public List<PurchaseOrderResponse> inbound() {
        return warehouseService.inbound();
    }

    @PostMapping("/purchase-orders/{id}/receive")
    @PreAuthorize("hasAnyAuthority('ADMIN','WAREHOUSE')")
    public PurchaseOrderResponse receive(@PathVariable String id, @Valid @RequestBody ReceivePurchaseOrderRequest request) {
        return warehouseService.receive(id, request);
    }
}

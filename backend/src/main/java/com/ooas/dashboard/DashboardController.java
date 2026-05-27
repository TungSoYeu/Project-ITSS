package com.ooas.dashboard;

import com.ooas.domain.POStatus;
import com.ooas.domain.RequestStatus;
import com.ooas.repository.OrderRequestRepository;
import com.ooas.repository.PurchaseOrderRepository;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final OrderRequestRepository orderRequestRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;

    public DashboardController(OrderRequestRepository orderRequestRepository, PurchaseOrderRepository purchaseOrderRepository) {
        this.orderRequestRepository = orderRequestRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
    }

    @GetMapping("/summary")
    public Map<String, Long> summary() {
        return Map.of(
                "pendingRequests", orderRequestRepository.countByStatus(RequestStatus.PENDING),
                "processingRequests", orderRequestRepository.countByStatus(RequestStatus.PROCESSING),
                "shippingOrders", purchaseOrderRepository.countByStatusIn(List.of(POStatus.PREPARING, POStatus.SHIPPING)),
                "warehouseInboundOrders", purchaseOrderRepository.countByStatusIn(List.of(POStatus.SHIPPING, POStatus.ARRIVED))
        );
    }
}

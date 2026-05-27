package com.ooas.orders;

import com.ooas.domain.RequestStatus;
import com.ooas.orders.dto.CancelRequest;
import com.ooas.orders.dto.OrderRequestResponse;
import com.ooas.orders.dto.OrderRequestUpsertRequest;
import com.ooas.security.JwtPrincipal;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/order-requests")
public class OrderRequestController {

    private final OrderRequestService orderRequestService;

    public OrderRequestController(OrderRequestService orderRequestService) {
        this.orderRequestService = orderRequestService;
    }

    @GetMapping
    public List<OrderRequestResponse> list(
            @RequestParam(required = false) RequestStatus status,
            @RequestParam(required = false) String search
    ) {
        return orderRequestService.list(status, search);
    }

    @GetMapping("/{id}")
    public OrderRequestResponse detail(@PathVariable String id) {
        return orderRequestService.detail(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN','SALES')")
    public OrderRequestResponse create(
            @Valid @RequestBody OrderRequestUpsertRequest request,
            @AuthenticationPrincipal JwtPrincipal principal
    ) {
        return orderRequestService.create(request, principal.id());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','SALES')")
    public OrderRequestResponse update(@PathVariable String id, @Valid @RequestBody OrderRequestUpsertRequest request) {
        return orderRequestService.update(id, request);
    }

    @PatchMapping("/{id}/submit")
    @PreAuthorize("hasAnyAuthority('ADMIN','SALES')")
    public OrderRequestResponse submit(@PathVariable String id) {
        return orderRequestService.submit(id);
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyAuthority('ADMIN','SALES','OVERSEAS_ORDER')")
    public OrderRequestResponse cancel(@PathVariable String id, @Valid @RequestBody CancelRequest request) {
        return orderRequestService.cancel(id, request);
    }
}

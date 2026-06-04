package com.ooas.service.impl;

import com.ooas.service.PurchaseOrderService;

import com.ooas.service.AuthService;
import com.ooas.exception.ApiException;
import com.ooas.dto.OrderRequest;
import com.ooas.entity.POStatus;
import com.ooas.entity.PurchaseOrder;
import com.ooas.entity.PurchaseOrderItem;
import com.ooas.entity.SiteInventory;
import com.ooas.entity.TransportMethod;
import com.ooas.entity.UserAccount;
import com.ooas.service.OptimizationService;
import com.ooas.dto.AllocationResponse;
import com.ooas.dto.OptimizationResponse;
import com.ooas.service.OrderRequestService;
import com.ooas.dto.CancelRequest;
import com.ooas.dto.PurchaseOrderResponse;
import com.ooas.dto.UpdatePoStatusRequest;
import com.ooas.repository.PurchaseOrderRepository;
import com.ooas.repository.SiteInventoryRepository;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class PurchaseOrderServiceImpl implements PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SiteInventoryRepository inventoryRepository;
    private final OrderRequestService orderRequestService;
    private final OptimizationService optimizationService;
    private final AuthService authService;

    public PurchaseOrderServiceImpl(
            PurchaseOrderRepository purchaseOrderRepository,
            SiteInventoryRepository inventoryRepository,
            OrderRequestService orderRequestService,
            OptimizationService optimizationService,
            AuthService authService
    ) {
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.inventoryRepository = inventoryRepository;
        this.orderRequestService = orderRequestService;
        this.optimizationService = optimizationService;
        this.authService = authService;
    }

    @Transactional(readOnly = true)
    public List<PurchaseOrderResponse> list(POStatus status, String siteId, String search) {
        String normalizedSearch = StringUtils.hasText(search) ? search.trim() : "";
        String normalizedSiteId = StringUtils.hasText(siteId) ? siteId.trim() : null;
        return purchaseOrderRepository.search(status, normalizedSiteId, normalizedSearch).stream()
                .map(po -> PurchaseOrderResponse.from(po, true))
                .toList();
    }

    @Transactional(readOnly = true)
    public PurchaseOrderResponse detail(String id) {
        return PurchaseOrderResponse.from(requirePurchaseOrder(id), true);
    }

    @Transactional
    public List<PurchaseOrderResponse> generateFromRequest(String requestId, @org.springframework.lang.NonNull String userId) {
        UserAccount currentUser = authService.requireUser(userId);
        OrderRequest request = orderRequestService.markProcessing(requestId, currentUser);
        OptimizationResponse optimization = optimizationService.optimize(request);
        if (!optimization.warnings().isEmpty()) {
            throw ApiException.badRequest("Khong the tao PO vi con canh bao thieu hang: " + String.join("; ", optimization.warnings()));
        }
        if (optimization.allocations().isEmpty()) {
            throw ApiException.badRequest("Khong co phuong an phan bo de tao PO");
        }

        Map<PoKey, PurchaseOrder> purchaseOrders = new LinkedHashMap<>();
        for (AllocationResponse allocation : optimization.allocations()) {
            PoKey key = new PoKey(allocation.siteId(), allocation.transportMethod());
            PurchaseOrder po = purchaseOrders.computeIfAbsent(key, ignored -> {
                PurchaseOrder created = new PurchaseOrder();
                created.setCode(generateCode());
                created.setRequest(request);
                created.setCreatedBy(currentUser);
                created.setTransportMethod(allocation.transportMethod());
                created.setStatus(POStatus.PENDING_CONFIRM);
                created.setExpectedArrivalDate(allocation.expectedArrivalDate());
                return created;
            });
            if (po.getSite() == null) {
                SiteInventory inventory = requireInventory(allocation.siteId(), allocation.skuId());
                po.setSite(inventory.getSite());
            }
            if (allocation.expectedArrivalDate().isAfter(po.getExpectedArrivalDate())) {
                po.setExpectedArrivalDate(allocation.expectedArrivalDate());
            }
            po.addItem(new PurchaseOrderItem(requireInventory(allocation.siteId(), allocation.skuId()).getSku(), allocation.quantity()));
            reduceInventory(allocation);
        }

        List<PurchaseOrder> saved = purchaseOrderRepository.saveAll(java.util.Objects.requireNonNull(List.copyOf(purchaseOrders.values())));
        orderRequestService.markOrdered(request);
        return saved.stream().map(po -> PurchaseOrderResponse.from(po, true)).toList();
    }

    @Transactional
    public PurchaseOrderResponse updateStatus(String id, UpdatePoStatusRequest request) {
        PurchaseOrder po = requirePurchaseOrder(id);
        po.setStatus(request.status());
        if (request.actualArrivalDate() != null) {
            po.setActualArrivalDate(request.actualArrivalDate());
        }
        if (request.status() == POStatus.ARRIVED && po.getActualArrivalDate() == null) {
            po.setActualArrivalDate(LocalDate.now());
        }
        return PurchaseOrderResponse.from(po, true);
    }

    @Transactional
    public PurchaseOrderResponse cancel(String id, CancelRequest request) {
        PurchaseOrder po = requirePurchaseOrder(id);
        if (po.getStatus() == POStatus.COMPLETED) {
            throw ApiException.badRequest("Khong the huy PO da hoan tat");
        }
        po.setStatus(POStatus.CANCELLED);
        po.setCancelReason(request.reason().trim());
        return PurchaseOrderResponse.from(po, true);
    }

    public PurchaseOrder requirePurchaseOrder(String id) {
        return purchaseOrderRepository.findWithItemsById(id)
                .orElseThrow(() -> ApiException.notFound("Khong tim thay don hang"));
    }

    private void reduceInventory(AllocationResponse allocation) {
        SiteInventory inventory = requireInventory(allocation.siteId(), allocation.skuId());
        if (inventory.getQuantity() < allocation.quantity()) {
            throw ApiException.badRequest("Ton kho khong du cho SKU " + allocation.skuCode() + " tai site " + allocation.siteCode());
        }
        inventory.setQuantity(inventory.getQuantity() - allocation.quantity());
    }

    private SiteInventory requireInventory(String siteId, String skuId) {
        return inventoryRepository.findBySiteIdAndSkuId(siteId, skuId)
                .orElseThrow(() -> ApiException.notFound("Khong tim thay ton kho site/SKU"));
    }

    private String generateCode() {
        String prefix = "PO-" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String code;
        do {
            code = prefix + "-" + Long.toString(System.nanoTime()).substring(8);
        } while (purchaseOrderRepository.existsByCode(code));
        return code;
    }

    private record PoKey(String siteId, TransportMethod transportMethod) {
    }
}

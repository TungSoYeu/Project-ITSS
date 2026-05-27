package com.ooas.service.impl;

import com.ooas.service.ShippingService;

import com.ooas.service.AuthService;
import com.ooas.exception.ApiException;
import com.ooas.entity.POStatus;
import com.ooas.entity.PurchaseOrder;
import com.ooas.entity.ShipmentTracking;
import com.ooas.entity.UserAccount;
import com.ooas.service.PurchaseOrderService;
import com.ooas.dto.PurchaseOrderResponse;
import com.ooas.repository.PurchaseOrderRepository;
import com.ooas.repository.ShipmentTrackingRepository;
import com.ooas.dto.TrackingRequest;
import com.ooas.dto.TrackingResponse;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ShippingServiceImpl implements ShippingService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final ShipmentTrackingRepository trackingRepository;
    private final PurchaseOrderService purchaseOrderService;
    private final AuthService authService;

    public ShippingServiceImpl(
            PurchaseOrderRepository purchaseOrderRepository,
            ShipmentTrackingRepository trackingRepository,
            PurchaseOrderService purchaseOrderService,
            AuthService authService
    ) {
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.trackingRepository = trackingRepository;
        this.purchaseOrderService = purchaseOrderService;
        this.authService = authService;
    }

    @Transactional(readOnly = true)
    public List<PurchaseOrderResponse> inTransit() {
        return purchaseOrderRepository.findByStatusInOrderByExpectedArrivalDateAsc(List.of(POStatus.PREPARING, POStatus.SHIPPING, POStatus.ARRIVED)).stream()
                .map(po -> PurchaseOrderResponse.from(po, false))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TrackingResponse> history(String purchaseOrderId) {
        purchaseOrderService.requirePurchaseOrder(purchaseOrderId);
        return trackingRepository.findByPurchaseOrderIdOrderByTimestampDesc(purchaseOrderId).stream()
                .map(TrackingResponse::from)
                .toList();
    }

    @Transactional
    public TrackingResponse addTracking(String purchaseOrderId, TrackingRequest request, @org.springframework.lang.NonNull String userId) {
        PurchaseOrder po = purchaseOrderService.requirePurchaseOrder(purchaseOrderId);
        if (po.getStatus() == POStatus.CANCELLED || po.getStatus() == POStatus.COMPLETED) {
            throw ApiException.badRequest("PO khong con o trang thai cap nhat van chuyen");
        }
        UserAccount updatedBy = authService.requireUser(userId);
        po.setStatus(request.status());
        if (request.status() == POStatus.ARRIVED && po.getActualArrivalDate() == null) {
            po.setActualArrivalDate(LocalDate.now());
        }
        ShipmentTracking tracking = new ShipmentTracking(
                po,
                request.status(),
                request.location(),
                request.notes(),
                request.evidenceFileUrl(),
                updatedBy
        );
        trackingRepository.save(tracking);
        return TrackingResponse.from(tracking);
    }
}

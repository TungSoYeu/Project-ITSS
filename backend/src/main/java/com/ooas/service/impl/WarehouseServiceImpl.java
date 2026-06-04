package com.ooas.service.impl;

import com.ooas.service.WarehouseService;

import com.ooas.exception.ApiException;
import com.ooas.entity.POStatus;
import com.ooas.entity.PurchaseOrder;
import com.ooas.entity.PurchaseOrderItem;
import com.ooas.service.PurchaseOrderService;
import com.ooas.dto.PurchaseOrderResponse;
import com.ooas.repository.PurchaseOrderRepository;
import com.ooas.dto.ReceivePurchaseOrderRequest;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WarehouseServiceImpl implements WarehouseService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderService purchaseOrderService;

    public WarehouseServiceImpl(PurchaseOrderRepository purchaseOrderRepository, PurchaseOrderService purchaseOrderService) {
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.purchaseOrderService = purchaseOrderService;
    }

    @Transactional(readOnly = true)
    public List<PurchaseOrderResponse> inbound() {
        return purchaseOrderRepository.findByStatusInOrderByExpectedArrivalDateAsc(List.of(POStatus.SHIPPING, POStatus.ARRIVED)).stream()
                .map(po -> PurchaseOrderResponse.from(po, false))
                .toList();
    }

    @Transactional
    public PurchaseOrderResponse receive(String purchaseOrderId, ReceivePurchaseOrderRequest request) {
        PurchaseOrder po = purchaseOrderService.requirePurchaseOrder(purchaseOrderId);
        if (po.getStatus() == POStatus.CANCELLED) {
            throw ApiException.badRequest("Khong the nhap kho PO da huy");
        }
        if (po.getStatus() == POStatus.COMPLETED) {
            throw ApiException.badRequest("PO da duoc nhap kho truoc do");
        }
        if (po.getStatus() != POStatus.SHIPPING && po.getStatus() != POStatus.ARRIVED) {
            throw ApiException.badRequest("Chi co the kiem hang PO dang van chuyen hoac da den kho");
        }

        Map<String, PurchaseOrderItem> itemsById = po.getItems().stream()
                .collect(Collectors.toMap(PurchaseOrderItem::getId, Function.identity()));

        request.items().forEach(received -> {
            PurchaseOrderItem item = itemsById.get(received.purchaseOrderItemId());
            if (item == null) {
                throw ApiException.badRequest("PO item khong thuoc don hang nay");
            }
            item.setQuantityReceived(received.quantityReceived());
            item.setDifference(received.quantityReceived() - item.getQuantityOrdered());
            item.setNotes(received.notes());
            if (item.getDifference() != 0 && (received.notes() == null || received.notes().isBlank())) {
                throw ApiException.badRequest("Can nhap ghi chu cho dong hang co chenh lech");
            }
        });

        po.setStatus(POStatus.COMPLETED);
        po.setActualArrivalDate(request.actualArrivalDate() == null ? LocalDate.now() : request.actualArrivalDate());
        return PurchaseOrderResponse.from(po, true);
    }
}

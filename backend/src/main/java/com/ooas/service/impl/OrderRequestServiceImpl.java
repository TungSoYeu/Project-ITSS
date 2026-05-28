package com.ooas.service.impl;

import com.ooas.service.OrderRequestService;

import com.ooas.service.AuthService;
import com.ooas.service.CatalogService;
import com.ooas.exception.ApiException;
import com.ooas.dto.OrderRequest;
import com.ooas.entity.OrderRequestItem;
import com.ooas.entity.RequestStatus;
import com.ooas.entity.UserAccount;
import com.ooas.dto.CancelRequest;
import com.ooas.dto.OrderRequestResponse;
import com.ooas.dto.OrderRequestUpsertRequest;
import com.ooas.repository.OrderRequestRepository;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class OrderRequestServiceImpl implements OrderRequestService {

    private final OrderRequestRepository requestRepository;
    private final AuthService authService;
    private final CatalogService catalogService;

    public OrderRequestServiceImpl(OrderRequestRepository requestRepository, AuthService authService, CatalogService catalogService) {
        this.requestRepository = requestRepository;
        this.authService = authService;
        this.catalogService = catalogService;
    }

    @Transactional(readOnly = true)
    public List<OrderRequestResponse> list(RequestStatus status, String search) {
        String normalizedSearch = StringUtils.hasText(search) ? search.trim() : "";
        return requestRepository.search(status, normalizedSearch).stream()
                .map(request -> OrderRequestResponse.from(request, false))
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderRequestResponse detail(String id) {
        return OrderRequestResponse.from(requireRequestWithItems(id), true);
    }

    @Transactional
    public OrderRequestResponse create(OrderRequestUpsertRequest request, @org.springframework.lang.NonNull String userId) {
        validateNoDuplicateSku(request);
        UserAccount currentUser = authService.requireUser(userId);

        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setCode(generateCode());
        orderRequest.setExpectedDate(request.expectedDate());
        orderRequest.setNotes(request.notes());
        orderRequest.setStatus(RequestStatus.DRAFT);
        orderRequest.setCreatedBy(currentUser);
        orderRequest.replaceItems(toItems(request));

        requestRepository.save(orderRequest);
        return OrderRequestResponse.from(orderRequest, true);
    }

    @Transactional
    public OrderRequestResponse update(String id, OrderRequestUpsertRequest request) {
        validateNoDuplicateSku(request);
        OrderRequest orderRequest = requireRequestWithItems(id);
        if (orderRequest.getStatus() != RequestStatus.DRAFT && orderRequest.getStatus() != RequestStatus.PENDING) {
            throw ApiException.badRequest("Chi co the sua yeu cau o trang thai Nhap hoac Cho xu ly");
        }
        orderRequest.setExpectedDate(request.expectedDate());
        orderRequest.setNotes(request.notes());
        orderRequest.replaceItems(toItems(request));
        return OrderRequestResponse.from(orderRequest, true);
    }

    @Transactional
    public OrderRequestResponse submit(String id) {
        OrderRequest orderRequest = requireRequestWithItems(id);
        if (orderRequest.getStatus() != RequestStatus.DRAFT) {
            throw ApiException.badRequest("Chi co the gui yeu cau dang o trang thai Nhap");
        }
        orderRequest.setStatus(RequestStatus.PENDING);
        return OrderRequestResponse.from(orderRequest, true);
    }

    @Transactional
    public OrderRequestResponse cancel(String id, CancelRequest request) {
        OrderRequest orderRequest = requireRequestWithItems(id);
        if (orderRequest.getStatus() == RequestStatus.ORDERED) {
            throw ApiException.badRequest("Khong the huy yeu cau da chot PO");
        }
        orderRequest.setStatus(RequestStatus.CANCELLED);
        orderRequest.setCancelReason(request.reason().trim());
        return OrderRequestResponse.from(orderRequest, true);
    }

    @Transactional
    public OrderRequest markProcessing(String id, UserAccount processedBy) {
        OrderRequest orderRequest = requireRequestWithItems(id);
        if (orderRequest.getStatus() == RequestStatus.CANCELLED || orderRequest.getStatus() == RequestStatus.ORDERED) {
            throw ApiException.badRequest("Yeu cau khong con o trang thai co the xu ly");
        }
        orderRequest.setStatus(RequestStatus.PROCESSING);
        orderRequest.setProcessedBy(processedBy);
        return orderRequest;
    }

    @Transactional
    public void markOrdered(OrderRequest request) {
        request.setStatus(RequestStatus.ORDERED);
    }

    public OrderRequest requireRequestWithItems(String id) {
        return requestRepository.findWithItemsById(id)
                .orElseThrow(() -> ApiException.notFound("Khong tim thay yeu cau nhap hang"));
    }

    private List<OrderRequestItem> toItems(OrderRequestUpsertRequest request) {
        return request.items().stream()
                .map(item -> new OrderRequestItem(
                        catalogService.requireSku(java.util.Objects.requireNonNull(item.skuId())),
                        item.quantity(),
                        item.expectedDate() == null ? request.expectedDate() : item.expectedDate()
                ))
                .toList();
    }

    private void validateNoDuplicateSku(OrderRequestUpsertRequest request) {
        Set<String> skuIds = new HashSet<>();
        request.items().forEach(item -> {
            if (!skuIds.add(item.skuId())) {
                throw ApiException.badRequest("Khong duoc lap SKU trong cung mot yeu cau");
            }
        });
    }

    private String generateCode() {
        String prefix = "YC-" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String code;
        do {
            code = prefix + "-" + Long.toString(System.currentTimeMillis()).substring(6);
        } while (requestRepository.existsByCode(code));
        return code;
    }
}

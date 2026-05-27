package com.ooas.service;

import com.ooas.dto.OrderRequest;
import com.ooas.entity.RequestStatus;
import com.ooas.entity.UserAccount;
import com.ooas.dto.CancelRequest;
import com.ooas.dto.OrderRequestResponse;
import com.ooas.dto.OrderRequestUpsertRequest;
import java.util.List;

public interface OrderRequestService {
    List<OrderRequestResponse> list(RequestStatus status, String search);
    OrderRequestResponse detail(String id);
    OrderRequestResponse create(OrderRequestUpsertRequest request, @org.springframework.lang.NonNull String userId);
    OrderRequestResponse update(String id, OrderRequestUpsertRequest request);
    OrderRequestResponse submit(String id);
    OrderRequestResponse cancel(String id, CancelRequest request);
    OrderRequest markProcessing(String id, UserAccount processedBy);
    void markOrdered(OrderRequest request);
    OrderRequest requireRequestWithItems(String id);
}

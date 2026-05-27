package com.ooas.optimization;

import com.ooas.domain.OrderRequest;
import com.ooas.domain.OrderRequestItem;
import com.ooas.domain.SiteInventory;
import com.ooas.domain.TransportMethod;
import com.ooas.optimization.dto.AllocationResponse;
import com.ooas.optimization.dto.CandidateResponse;
import com.ooas.optimization.dto.InventoryCheckResponse;
import com.ooas.optimization.dto.OptimizationResponse;
import com.ooas.orders.OrderRequestService;
import com.ooas.repository.SiteInventoryRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OptimizationService {

    private final OrderRequestService orderRequestService;
    private final SiteInventoryRepository inventoryRepository;

    public OptimizationService(OrderRequestService orderRequestService, SiteInventoryRepository inventoryRepository) {
        this.orderRequestService = orderRequestService;
        this.inventoryRepository = inventoryRepository;
    }

    @Transactional(readOnly = true)
    public InventoryCheckResponse checkInventory(String requestId) {
        OrderRequest request = orderRequestService.requireRequestWithItems(requestId);
        List<CandidateResponse> candidates = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (OrderRequestItem item : request.getItems()) {
            LocalDate needDate = item.getExpectedDate() == null ? request.getExpectedDate() : item.getExpectedDate();
            List<SupplyCandidate> feasibleCandidates = buildCandidates(item, needDate, today);
            int feasibleQuantity = feasibleCandidates.stream().mapToInt(SupplyCandidate::availableQuantity).sum();
            if (feasibleQuantity < item.getQuantity()) {
                warnings.add("SKU " + item.getSku().getCode() + " thieu " + (item.getQuantity() - feasibleQuantity) + " " + item.getSku().getUnit());
            }
            feasibleCandidates.forEach(candidate -> candidates.add(toCandidateResponse(item, candidate, true)));
        }

        return new InventoryCheckResponse(request.getId(), request.getCode(), candidates, warnings);
    }

    @Transactional(readOnly = true)
    public OptimizationResponse optimize(String requestId) {
        OrderRequest request = orderRequestService.requireRequestWithItems(requestId);
        return optimize(request);
    }

    public OptimizationResponse optimize(OrderRequest request) {
        List<AllocationResponse> allocations = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (OrderRequestItem item : request.getItems()) {
            LocalDate needDate = item.getExpectedDate() == null ? request.getExpectedDate() : item.getExpectedDate();
            List<SupplyCandidate> candidates = buildCandidates(item, needDate, today).stream()
                    .sorted(Comparator
                            .comparing(SupplyCandidate::transportMethod)
                            .thenComparing(SupplyCandidate::availableQuantity, Comparator.reverseOrder()))
                    .toList();

            int feasibleQuantity = candidates.stream().mapToInt(SupplyCandidate::availableQuantity).sum();
            if (feasibleQuantity < item.getQuantity()) {
                warnings.add("SKU " + item.getSku().getCode() + " khong du nguon cung kha thi. Thieu " + (item.getQuantity() - feasibleQuantity));
            }

            int remaining = item.getQuantity();
            for (SupplyCandidate candidate : candidates) {
                if (remaining <= 0) {
                    break;
                }
                int quantity = Math.min(remaining, candidate.availableQuantity());
                remaining -= quantity;
                allocations.add(new AllocationResponse(
                        item.getSku().getId(),
                        item.getSku().getCode(),
                        item.getSku().getName(),
                        candidate.inventory().getSite().getId(),
                        candidate.inventory().getSite().getCode(),
                        candidate.inventory().getSite().getName(),
                        candidate.transportMethod(),
                        quantity,
                        candidate.expectedArrivalDate()
                ));
            }
        }

        return new OptimizationResponse(request.getId(), request.getCode(), allocations, warnings);
    }

    private List<SupplyCandidate> buildCandidates(OrderRequestItem item, LocalDate needDate, LocalDate today) {
        return inventoryRepository.findAvailableBySkuId(item.getSku().getId()).stream()
                .map(inventory -> toCandidate(inventory, needDate, today))
                .flatMap(List::stream)
                .toList();
    }

    private List<SupplyCandidate> toCandidate(SiteInventory inventory, LocalDate needDate, LocalDate today) {
        LocalDate seaArrival = today.plusDays(inventory.getSite().getSeaLeadTime());
        if (!seaArrival.isAfter(needDate)) {
            return List.of(new SupplyCandidate(inventory, TransportMethod.SEA, inventory.getQuantity(), seaArrival));
        }
        LocalDate airArrival = today.plusDays(inventory.getSite().getAirLeadTime());
        if (!airArrival.isAfter(needDate)) {
            return List.of(new SupplyCandidate(inventory, TransportMethod.AIR, inventory.getQuantity(), airArrival));
        }
        return List.of();
    }

    private CandidateResponse toCandidateResponse(OrderRequestItem item, SupplyCandidate candidate, boolean feasible) {
        return new CandidateResponse(
                item.getSku().getId(),
                item.getSku().getCode(),
                item.getSku().getName(),
                item.getQuantity(),
                candidate.inventory().getSite().getId(),
                candidate.inventory().getSite().getCode(),
                candidate.inventory().getSite().getName(),
                candidate.availableQuantity(),
                candidate.transportMethod(),
                candidate.transportMethod() == TransportMethod.SEA
                        ? candidate.inventory().getSite().getSeaLeadTime()
                        : candidate.inventory().getSite().getAirLeadTime(),
                candidate.expectedArrivalDate(),
                feasible
        );
    }

    private record SupplyCandidate(
            SiteInventory inventory,
            TransportMethod transportMethod,
            int availableQuantity,
            LocalDate expectedArrivalDate
    ) {
    }
}

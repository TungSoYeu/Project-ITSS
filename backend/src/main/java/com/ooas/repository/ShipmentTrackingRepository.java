package com.ooas.repository;

import com.ooas.domain.ShipmentTracking;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShipmentTrackingRepository extends JpaRepository<ShipmentTracking, String> {

    @EntityGraph(attributePaths = {"updatedBy"})
    List<ShipmentTracking> findByPurchaseOrderIdOrderByTimestampDesc(String purchaseOrderId);
}

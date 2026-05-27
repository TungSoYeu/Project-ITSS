package com.ooas.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "shipment_trackings")
public class ShipmentTracking extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "purchase_order_id", nullable = false)
    private PurchaseOrder purchaseOrder;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private POStatus status;

    private String location;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "evidence_file_url")
    private String evidenceFileUrl;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "updated_by_id", nullable = false)
    private UserAccount updatedBy;

    @Column(nullable = false)
    private Instant timestamp;

    public ShipmentTracking(PurchaseOrder purchaseOrder, POStatus status, String location, String notes, String evidenceFileUrl, UserAccount updatedBy) {
        this.purchaseOrder = purchaseOrder;
        this.status = status;
        this.location = location;
        this.notes = notes;
        this.evidenceFileUrl = evidenceFileUrl;
        this.updatedBy = updatedBy;
    }

    @Override
    @PrePersist
    public void prePersist() {
        super.prePersist();
        if (timestamp == null) {
            timestamp = java.time.Instant.now();
        }
    }
}

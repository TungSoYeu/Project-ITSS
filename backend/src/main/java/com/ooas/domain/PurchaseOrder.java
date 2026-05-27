package com.ooas.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "purchase_orders")
public class PurchaseOrder extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "request_id", nullable = false)
    private OrderRequest request;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "site_id", nullable = false)
    private Site site;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by_id", nullable = false)
    private UserAccount createdBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "transport_method", nullable = false, length = 16)
    private TransportMethod transportMethod = TransportMethod.SEA;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private POStatus status = POStatus.PENDING_CONFIRM;

    @Column(name = "expected_arrival_date", nullable = false)
    private LocalDate expectedArrivalDate;

    @Column(name = "actual_arrival_date")
    private LocalDate actualArrivalDate;

    @Column(name = "cancel_reason", columnDefinition = "TEXT")
    private String cancelReason;

    @OneToMany(mappedBy = "purchaseOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PurchaseOrderItem> items = new ArrayList<>();

    public void addItem(PurchaseOrderItem item) {
        item.setPurchaseOrder(this);
        items.add(item);
    }
}

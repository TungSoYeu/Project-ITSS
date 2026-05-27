package com.ooas.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "purchase_order_items",
        uniqueConstraints = @UniqueConstraint(name = "uk_purchase_order_item_po_sku", columnNames = {"purchase_order_id", "sku_id"})
)
public class PurchaseOrderItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "purchase_order_id", nullable = false)
    private PurchaseOrder purchaseOrder;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sku_id", nullable = false)
    private Sku sku;

    @Column(name = "quantity_ordered", nullable = false)
    private int quantityOrdered;

    @Column(name = "quantity_received", nullable = false)
    private int quantityReceived;

    @Column(nullable = false)
    private int difference;

    @Column(columnDefinition = "TEXT")
    private String notes;

    public PurchaseOrderItem(Sku sku, int quantityOrdered) {
        this.sku = sku;
        this.quantityOrdered = quantityOrdered;
        this.quantityReceived = 0;
        this.difference = 0;
    }
}

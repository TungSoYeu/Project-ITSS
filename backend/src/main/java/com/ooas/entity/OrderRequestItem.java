package com.ooas.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import com.ooas.dto.OrderRequest;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "order_request_items",
        uniqueConstraints = @UniqueConstraint(name = "uk_order_request_item_request_sku", columnNames = {"request_id", "sku_id"})
)
public class OrderRequestItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "request_id", nullable = false)
    private OrderRequest request;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sku_id", nullable = false)
    private Sku sku;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "expected_date")
    private LocalDate expectedDate;

    public OrderRequestItem(Sku sku, int quantity, LocalDate expectedDate) {
        this.sku = sku;
        this.quantity = quantity;
        this.expectedDate = expectedDate;
    }
}

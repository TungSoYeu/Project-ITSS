package com.ooas.dto;

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
import com.ooas.entity.BaseEntity;
import com.ooas.entity.OrderRequestItem;
import com.ooas.entity.RequestStatus;
import com.ooas.entity.UserAccount;
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
@Table(name = "order_requests")
public class OrderRequest extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String code;

    @Column(name = "expected_date", nullable = false)
    private LocalDate expectedDate;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private RequestStatus status = RequestStatus.DRAFT;

    @Column(name = "cancel_reason", columnDefinition = "TEXT")
    private String cancelReason;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by_id", nullable = false)
    private UserAccount createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processed_by_id")
    private UserAccount processedBy;

    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderRequestItem> items = new ArrayList<>();

    public void replaceItems(List<OrderRequestItem> newItems) {
        items.clear();
        newItems.forEach(item -> {
            item.setRequest(this);
            items.add(item);
        });
    }
}

package com.ooas.repository;

import com.ooas.entity.POStatus;
import com.ooas.entity.PurchaseOrder;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, String> {

    boolean existsByCode(String code);

    @EntityGraph(attributePaths = {"request", "site", "createdBy", "items", "items.sku"})
    Optional<PurchaseOrder> findWithItemsById(String id);

    @EntityGraph(attributePaths = {"request", "site", "items", "items.sku"})
    List<PurchaseOrder> findByStatusInOrderByExpectedArrivalDateAsc(Collection<POStatus> statuses);

    @Query("""
            select distinct po
            from PurchaseOrder po
            left join po.site site
            left join po.request req
            left join po.items item
            left join item.sku sku
            where (:status is null or po.status = :status)
              and (:siteId is null or site.id = :siteId)
              and (:search is null
                or lower(po.code) like lower(concat('%', :search, '%'))
                or lower(req.code) like lower(concat('%', :search, '%'))
                or lower(site.code) like lower(concat('%', :search, '%'))
                or lower(sku.code) like lower(concat('%', :search, '%')))
            order by po.createdAt desc
            """)
    List<PurchaseOrder> search(@Param("status") POStatus status, @Param("siteId") String siteId, @Param("search") String search);

    long countByStatusIn(Collection<POStatus> statuses);
}

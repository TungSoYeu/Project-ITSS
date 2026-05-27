package com.ooas.repository;

import com.ooas.dto.OrderRequest;
import com.ooas.entity.RequestStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRequestRepository extends JpaRepository<OrderRequest, String> {

    boolean existsByCode(String code);

    @EntityGraph(attributePaths = {"createdBy", "processedBy", "items", "items.sku"})
    Optional<OrderRequest> findWithItemsById(String id);

    @Query("""
            select distinct r
            from OrderRequest r
            left join r.items item
            left join item.sku sku
            where (:status is null or r.status = :status)
              and (:search is null
                or lower(r.code) like lower(concat('%', :search, '%'))
                or lower(sku.code) like lower(concat('%', :search, '%')))
            order by r.createdAt desc
            """)
    List<OrderRequest> search(@Param("status") RequestStatus status, @Param("search") String search);

    long countByStatus(RequestStatus status);
}

package com.ooas.repository;

import com.ooas.domain.SiteInventory;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SiteInventoryRepository extends JpaRepository<SiteInventory, String> {

    @EntityGraph(attributePaths = {"site", "sku"})
    List<SiteInventory> findBySiteIdOrderBySkuCodeAsc(String siteId);

    Optional<SiteInventory> findBySiteIdAndSkuId(String siteId, String skuId);

    @EntityGraph(attributePaths = {"site", "sku"})
    @Query("""
            select inv
            from SiteInventory inv
            where inv.sku.id = :skuId
              and inv.quantity > 0
              and inv.site.active = true
            order by inv.quantity desc
            """)
    List<SiteInventory> findAvailableBySkuId(@Param("skuId") String skuId);

    long countBySiteId(String siteId);
}

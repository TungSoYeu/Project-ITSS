package com.ooas.repository;

import com.ooas.entity.Sku;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SkuRepository extends JpaRepository<Sku, String> {

    Optional<Sku> findByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCase(String code);

    @Query("""
            select s
            from Sku s
            where :search is null
               or lower(s.code) like lower(concat('%', :search, '%'))
               or lower(s.name) like lower(concat('%', :search, '%'))
            order by s.code asc
            """)
    List<Sku> search(@Param("search") String search);
}

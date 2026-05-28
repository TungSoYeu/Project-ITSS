package com.ooas.repository;

import com.ooas.entity.Site;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SiteRepository extends JpaRepository<Site, String> {

    Optional<Site> findByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCase(String code);

    @Query("""
            select s
            from Site s
            where (:active is null or s.active = :active)
              and (:search is null
                or lower(s.code) like lower(concat('%', :search, '%'))
                or lower(s.name) like lower(concat('%', :search, '%'))
                or lower(s.country) like lower(concat('%', :search, '%')))
            order by s.code asc
            """)
    List<Site> search(@Param("active") Boolean active, @Param("search") String search);
}

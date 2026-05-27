package com.ooas.repository;

import com.ooas.entity.AccountStatus;
import com.ooas.entity.UserAccount;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserAccountRepository extends JpaRepository<UserAccount, String> {

    Optional<UserAccount> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmployeeIdIgnoreCase(String employeeId);

    @Query("""
            select u
            from UserAccount u
            where (:status is null or u.status = :status)
              and (:search is null
                or lower(u.fullName) like lower(concat('%', :search, '%'))
                or lower(u.email) like lower(concat('%', :search, '%'))
                or lower(u.employeeId) like lower(concat('%', :search, '%')))
            order by u.createdAt desc
            """)
    List<UserAccount> search(@Param("status") AccountStatus status, @Param("search") String search);
}

package com.ooas.auth.dto;

import com.ooas.domain.AccountStatus;
import com.ooas.domain.Role;
import com.ooas.domain.UserAccount;
import java.time.Instant;

public record UserResponse(
        String id,
        String email,
        String fullName,
        String employeeId,
        Role role,
        AccountStatus status,
        Instant createdAt
) {
    public static UserResponse from(UserAccount user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getEmployeeId(),
                user.getRole(),
                user.getStatus(),
                user.getCreatedAt()
        );
    }
}

package com.ooas.dto;

import com.ooas.entity.AccountStatus;
import com.ooas.entity.Role;
import com.ooas.entity.UserAccount;
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

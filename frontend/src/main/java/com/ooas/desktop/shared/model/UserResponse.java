package com.ooas.desktop.shared.model;

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
}


package com.ooas.auth.dto;

import com.ooas.domain.Role;
import jakarta.validation.constraints.NotNull;

public record UpdateRoleRequest(
        @NotNull(message = "Vai tro khong hop le")
        Role role
) {
}

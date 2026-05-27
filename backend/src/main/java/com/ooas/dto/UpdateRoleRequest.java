package com.ooas.dto;

import com.ooas.entity.Role;
import jakarta.validation.constraints.NotNull;

public record UpdateRoleRequest(
        @NotNull(message = "Vai tro khong hop le")
        Role role
) {
}

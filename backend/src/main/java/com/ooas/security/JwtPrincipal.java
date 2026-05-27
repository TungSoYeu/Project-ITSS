package com.ooas.security;

import com.ooas.entity.Role;

public record JwtPrincipal(
        String id,
        String email,
        Role role,
        String fullName
) {
}

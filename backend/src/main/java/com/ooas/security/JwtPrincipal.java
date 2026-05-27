package com.ooas.security;

import com.ooas.domain.Role;

public record JwtPrincipal(
        String id,
        String email,
        Role role,
        String fullName
) {
}

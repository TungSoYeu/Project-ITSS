package com.ooas.auth;

import com.ooas.auth.dto.AuthResponse;
import com.ooas.auth.dto.LoginRequest;
import com.ooas.auth.dto.RegisterRequest;
import com.ooas.auth.dto.UpdateRoleRequest;
import com.ooas.auth.dto.UserResponse;
import com.ooas.domain.AccountStatus;
import com.ooas.security.JwtPrincipal;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/auth/register")
    public Map<String, Object> register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/auth/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/auth/profile")
    public UserResponse getProfile(@AuthenticationPrincipal JwtPrincipal principal) {
        return authService.getProfile(principal.id());
    }

    @GetMapping("/admin/users")
    @PreAuthorize("hasAuthority('ADMIN')")
    public List<UserResponse> getAllUsers(
            @RequestParam(required = false) AccountStatus status,
            @RequestParam(required = false) String search
    ) {
        return authService.getAllUsers(status, search);
    }

    @PatchMapping("/admin/users/{id}/approve")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Map<String, Object> approveUser(@PathVariable String id) {
        return authService.approveUser(id);
    }

    @PatchMapping("/admin/users/{id}/block")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Map<String, Object> blockUser(@PathVariable String id) {
        return authService.blockUser(id);
    }

    @PatchMapping("/admin/users/{id}/role")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Map<String, Object> updateRole(@PathVariable String id, @Valid @RequestBody UpdateRoleRequest request) {
        return authService.updateRole(id, request.role());
    }
}

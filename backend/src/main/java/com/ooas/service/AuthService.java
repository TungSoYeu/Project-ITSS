package com.ooas.service;

import com.ooas.dto.AuthResponse;
import com.ooas.dto.LoginRequest;
import com.ooas.dto.RegisterRequest;
import com.ooas.dto.UserResponse;
import com.ooas.entity.AccountStatus;
import com.ooas.entity.Role;
import com.ooas.entity.UserAccount;
import java.util.List;
import java.util.Map;
import org.springframework.lang.NonNull;

public interface AuthService {
    Map<String, Object> register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    UserAccount requireUser(@NonNull String userId);
    UserResponse getProfile(@NonNull String userId);
    List<UserResponse> getAllUsers(AccountStatus status, String search);
    Map<String, Object> approveUser(@NonNull String userId);
    Map<String, Object> blockUser(@NonNull String userId);
    Map<String, Object> updateRole(@NonNull String userId, Role role);
    Map<String, Object> updateProfile(@NonNull String userId, com.ooas.dto.UpdateProfileRequest request);
    Map<String, Object> changePassword(@NonNull String userId, com.ooas.dto.ChangePasswordRequest request);
}

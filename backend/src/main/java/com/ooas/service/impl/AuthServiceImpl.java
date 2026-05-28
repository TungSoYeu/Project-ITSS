package com.ooas.service.impl;

import com.ooas.service.AuthService;

import com.ooas.dto.AuthResponse;
import com.ooas.dto.LoginRequest;
import com.ooas.dto.RegisterRequest;
import com.ooas.dto.UserResponse;
import com.ooas.exception.ApiException;
import com.ooas.entity.AccountStatus;
import com.ooas.entity.Role;
import com.ooas.entity.UserAccount;
import com.ooas.repository.UserAccountRepository;
import com.ooas.service.JwtService;
import java.util.List;
import java.util.Map;
import org.springframework.lang.NonNull;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserAccountRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthServiceImpl(UserAccountRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public Map<String, Object> register(RegisterRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw ApiException.conflict("Email da duoc dang ky");
        }
        if (userRepository.existsByEmployeeIdIgnoreCase(request.employeeId())) {
            throw ApiException.conflict("Ma nhan vien da ton tai");
        }

        UserAccount user = new UserAccount(
                request.email().trim().toLowerCase(),
                passwordEncoder.encode(request.password()),
                request.fullName().trim(),
                request.employeeId().trim(),
                request.role(),
                AccountStatus.PENDING
        );
        userRepository.save(user);

        return Map.of(
                "message", "Dang ky tai khoan thanh cong. Vui long cho phe duyet tu Ban quan tri.",
                "user", UserResponse.from(user)
        );
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        UserAccount user = userRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> ApiException.unauthorized("Email hoac mat khau khong chinh xac"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw ApiException.unauthorized("Email hoac mat khau khong chinh xac");
        }
        if (user.getStatus() == AccountStatus.PENDING) {
            throw ApiException.forbidden("Tai khoan chua duoc phe duyet. Vui long cho Admin duyet.");
        }
        if (user.getStatus() == AccountStatus.BLOCKED) {
            throw ApiException.forbidden("Tai khoan da bi vo hieu hoa.");
        }

        return new AuthResponse(jwtService.generateToken(user), UserResponse.from(user));
    }

    @Transactional(readOnly = true)
    public UserAccount requireUser(@NonNull String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> ApiException.unauthorized("Token khong hop le hoac da het han"));
    }

    @Transactional(readOnly = true)
    public UserResponse getProfile(@NonNull String userId) {
        return UserResponse.from(requireUser(userId));
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers(AccountStatus status, String search) {
        String normalizedSearch = StringUtils.hasText(search) ? search.trim() : null;
        return userRepository.search(status, normalizedSearch).stream()
                .map(UserResponse::from)
                .toList();
    }

    @Transactional
    public Map<String, Object> approveUser(@NonNull String userId) {
        UserAccount user = userRepository.findById(userId)
                .orElseThrow(() -> ApiException.notFound("Khong tim thay tai khoan"));
        user.setStatus(AccountStatus.APPROVED);
        return Map.of("message", "Da phe duyet tai khoan " + user.getFullName(), "user", UserResponse.from(user));
    }

    @Transactional
    public Map<String, Object> blockUser(@NonNull String userId) {
        UserAccount user = userRepository.findById(userId)
                .orElseThrow(() -> ApiException.notFound("Khong tim thay tai khoan"));
        user.setStatus(AccountStatus.BLOCKED);
        return Map.of("message", "Da vo hieu hoa tai khoan " + user.getFullName(), "user", UserResponse.from(user));
    }

    @Transactional
    public Map<String, Object> updateRole(@NonNull String userId, Role role) {
        UserAccount user = userRepository.findById(userId)
                .orElseThrow(() -> ApiException.notFound("Khong tim thay tai khoan"));
        user.setRole(role);
        return Map.of("message", "Da cap nhat vai tro cho " + user.getFullName(), "user", UserResponse.from(user));
    }

    @Transactional
    public Map<String, Object> updateProfile(@NonNull String userId, com.ooas.dto.UpdateProfileRequest request) {
        UserAccount user = requireUser(userId);
        
        if (!user.getEmployeeId().equalsIgnoreCase(request.employeeId()) && 
            userRepository.existsByEmployeeIdIgnoreCase(request.employeeId())) {
            throw ApiException.conflict("Mã nhân viên đã tồn tại");
        }

        user.setFullName(request.fullName().trim());
        user.setEmployeeId(request.employeeId().trim());
        
        return Map.of(
            "message", "Cập nhật thông tin thành công",
            "user", UserResponse.from(user)
        );
    }

    @Transactional
    public Map<String, Object> changePassword(@NonNull String userId, com.ooas.dto.ChangePasswordRequest request) {
        UserAccount user = requireUser(userId);

        if (!passwordEncoder.matches(request.oldPassword(), user.getPassword())) {
            throw ApiException.badRequest("Mật khẩu hiện tại không chính xác");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));

        return Map.of("message", "Đổi mật khẩu thành công");
    }
}

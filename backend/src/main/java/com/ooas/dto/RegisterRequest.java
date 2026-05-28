package com.ooas.dto;

import com.ooas.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Ho va ten khong duoc de trong")
        String fullName,

        @NotBlank(message = "Email khong duoc de trong")
        @Email(message = "Email khong dung dinh dang")
        String email,

        @NotBlank(message = "Mat khau khong duoc de trong")
        @Size(min = 8, message = "Mat khau phai co it nhat 8 ky tu")
        String password,

        @NotBlank(message = "Ma nhan vien khong duoc de trong")
        String employeeId,

        @NotNull(message = "Bo phan khong hop le")
        Role role
) {
}

package com.ooas.model;

public record ChangePasswordRequest(String oldPassword, String newPassword) {
}

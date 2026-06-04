package com.ooas.desktop.shared.model;

public record ChangePasswordRequest(String oldPassword, String newPassword) {
}


package com.ooas.desktop.shared.model;

public record RegisterRequest(String fullName, String email, String password, String employeeId, Role role) {
}


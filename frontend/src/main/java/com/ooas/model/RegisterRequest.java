package com.ooas.model;

public record RegisterRequest(String fullName, String email, String password, String employeeId, Role role) {
}

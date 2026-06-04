package com.ooas.desktop.shared.exception;

public class DatabaseException extends RuntimeException {
    private final int statusCode;

    public DatabaseException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public int statusCode() {
        return statusCode;
    }
}


package com.ooas.config;

import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class EnvLoader {

    private EnvLoader() {
    }

    public static void load() {
        Path envFile = Path.of(".env");
        if (Files.isRegularFile(envFile)) {
            loadFile(envFile);
        }
        applySpringProperties();
    }

    private static void loadFile(Path path) {
        try {
            for (String line : Files.readAllLines(path, StandardCharsets.UTF_8)) {
                String trimmed = line.trim();
                if (trimmed.isBlank() || trimmed.startsWith("#")) {
                    continue;
                }
                int separator = trimmed.indexOf('=');
                if (separator <= 0) {
                    continue;
                }
                String key = trimmed.substring(0, separator).trim();
                String value = stripQuotes(trimmed.substring(separator + 1).trim());
                if (System.getProperty(key) == null && System.getenv(key) == null) {
                    System.setProperty(key, value);
                }
            }
        } catch (IOException ignored) {
            // Local .env support is optional.
        }
    }

    private static void applySpringProperties() {
        setSpringProperty("server.port", "PORT");
        setSpringProperty("app.jwt.secret", "JWT_SECRET");
        setSpringProperty("app.jwt.expiration", "JWT_EXPIRATION");
        setSpringProperty("spring.datasource.url", "SPRING_DATASOURCE_URL");
        setSpringProperty("spring.datasource.username", "SPRING_DATASOURCE_USERNAME");
        setSpringProperty("spring.datasource.password", "SPRING_DATASOURCE_PASSWORD");

        if (!hasProperty("spring.datasource.url")) {
            configureFromDatabaseUrl(configValue("DATABASE_URL"));
        }
    }

    private static void configureFromDatabaseUrl(String rawUrl) {
        if (rawUrl == null || rawUrl.isBlank()) {
            return;
        }
        String normalized = rawUrl.startsWith("postgres://")
                ? rawUrl.replaceFirst("postgres://", "postgresql://")
                : rawUrl;
        if (!normalized.startsWith("postgresql://")) {
            return;
        }

        URI uri = URI.create(normalized);
        StringBuilder jdbcUrl = new StringBuilder("jdbc:postgresql://")
                .append(uri.getHost());
        if (uri.getPort() > -1) {
            jdbcUrl.append(':').append(uri.getPort());
        }
        jdbcUrl.append(uri.getPath());

        String jdbcQuery = toJdbcQuery(uri.getRawQuery());
        if (!jdbcQuery.isBlank()) {
            jdbcUrl.append('?').append(jdbcQuery);
        }
        System.setProperty("spring.datasource.url", jdbcUrl.toString());

        if (uri.getRawUserInfo() != null) {
            String[] parts = uri.getRawUserInfo().split(":", 2);
            if (parts.length > 0 && !hasProperty("spring.datasource.username")) {
                System.setProperty("spring.datasource.username", decode(parts[0]));
            }
            if (parts.length > 1 && !hasProperty("spring.datasource.password")) {
                System.setProperty("spring.datasource.password", decode(parts[1]));
            }
        }
    }

    private static String toJdbcQuery(String rawQuery) {
        if (rawQuery == null || rawQuery.isBlank()) {
            return "";
        }
        List<String> pairs = new ArrayList<>();
        for (String pair : rawQuery.split("&")) {
            String[] parts = pair.split("=", 2);
            String key = decode(parts[0]);
            String value = parts.length > 1 ? decode(parts[1]) : "";
            pairs.add(("schema".equals(key) ? "currentSchema" : key) + "=" + value);
        }
        return String.join("&", pairs);
    }

    private static void setSpringProperty(String springProperty, String envKey) {
        String value = configValue(envKey);
        if (value != null && !hasProperty(springProperty)) {
            System.setProperty(springProperty, value);
        }
    }

    private static String configValue(String key) {
        String property = System.getProperty(key);
        return property != null ? property : System.getenv(key);
    }

    private static boolean hasProperty(String key) {
        return System.getProperty(key) != null || System.getenv(key) != null;
    }

    private static String stripQuotes(String value) {
        if (value.length() >= 2) {
            char first = value.charAt(0);
            char last = value.charAt(value.length() - 1);
            if ((first == '"' && last == '"') || (first == '\'' && last == '\'')) {
                return value.substring(1, value.length() - 1);
            }
        }
        return value;
    }

    private static String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }
}

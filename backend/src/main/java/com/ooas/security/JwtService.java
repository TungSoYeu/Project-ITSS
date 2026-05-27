package com.ooas.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ooas.common.ApiException;
import com.ooas.domain.Role;
import com.ooas.domain.UserAccount;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private static final String HMAC_SHA256 = "HmacSHA256";

    private final ObjectMapper objectMapper;
    private final String secret;
    private final Duration expiration;

    public JwtService(
            ObjectMapper objectMapper,
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration}") String expiration
    ) {
        this.objectMapper = objectMapper;
        this.secret = secret;
        this.expiration = parseDuration(expiration);
    }

    public String generateToken(UserAccount user) {
        Instant now = Instant.now();
        Map<String, Object> header = new LinkedHashMap<>();
        header.put("alg", "HS256");
        header.put("typ", "JWT");

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sub", user.getId());
        payload.put("email", user.getEmail());
        payload.put("role", user.getRole().name());
        payload.put("fullName", user.getFullName());
        payload.put("iat", now.getEpochSecond());
        payload.put("exp", now.plus(expiration).getEpochSecond());

        String encodedHeader = encodeJson(header);
        String encodedPayload = encodeJson(payload);
        String signingInput = encodedHeader + "." + encodedPayload;
        return signingInput + "." + encode(hmac(signingInput));
    }

    public JwtPrincipal parseToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw invalidToken();
            }

            String signingInput = parts[0] + "." + parts[1];
            byte[] expectedSignature = hmac(signingInput);
            byte[] providedSignature = decode(parts[2]);
            if (!MessageDigest.isEqual(expectedSignature, providedSignature)) {
                throw invalidToken();
            }

            JsonNode header = objectMapper.readTree(decode(parts[0]));
            if (!"HS256".equals(text(header, "alg"))) {
                throw invalidToken();
            }

            JsonNode payload = objectMapper.readTree(decode(parts[1]));
            if (payload.path("exp").asLong(0) <= Instant.now().getEpochSecond()) {
                throw invalidToken();
            }

            return new JwtPrincipal(
                    text(payload, "sub"),
                    text(payload, "email"),
                    Role.valueOf(text(payload, "role")),
                    text(payload, "fullName")
            );
        } catch (ApiException ex) {
            throw ex;
        } catch (Exception ex) {
            throw invalidToken();
        }
    }

    private String encodeJson(Map<String, Object> value) {
        try {
            return encode(objectMapper.writeValueAsBytes(value));
        } catch (Exception ex) {
            throw invalidToken();
        }
    }

    private byte[] hmac(String value) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256));
            return mac.doFinal(value.getBytes(StandardCharsets.UTF_8));
        } catch (Exception ex) {
            throw invalidToken();
        }
    }

    private String encode(byte[] value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value);
    }

    private byte[] decode(String value) {
        return Base64.getUrlDecoder().decode(value);
    }

    private String text(JsonNode node, String field) {
        String value = node.path(field).asText(null);
        if (value == null || value.isBlank()) {
            throw invalidToken();
        }
        return value;
    }

    private ApiException invalidToken() {
        return ApiException.unauthorized("Token khong hop le hoac da het han");
    }

    private Duration parseDuration(String raw) {
        String value = raw == null ? "" : raw.trim().toLowerCase();
        if (value.isBlank()) {
            return Duration.ofHours(24);
        }
        try {
            if (value.endsWith("ms")) {
                return Duration.ofMillis(Long.parseLong(value.substring(0, value.length() - 2)));
            }
            if (value.endsWith("s")) {
                return Duration.ofSeconds(Long.parseLong(value.substring(0, value.length() - 1)));
            }
            if (value.endsWith("m")) {
                return Duration.ofMinutes(Long.parseLong(value.substring(0, value.length() - 1)));
            }
            if (value.endsWith("h")) {
                return Duration.ofHours(Long.parseLong(value.substring(0, value.length() - 1)));
            }
            if (value.endsWith("d")) {
                return Duration.ofDays(Long.parseLong(value.substring(0, value.length() - 1)));
            }
            return Duration.parse(raw);
        } catch (RuntimeException ex) {
            return Duration.ofHours(24);
        }
    }
}

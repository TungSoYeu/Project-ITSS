package com.ooas.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AuthResponse(
        @JsonProperty("access_token") String accessToken,
        UserResponse user
) {
}

package com.ringdu.server.auth.dto;

public record TokenRefreshResponse(
        String accessToken,
        String tokenType
) {

    public static TokenRefreshResponse of(String accessToken) {
        return new TokenRefreshResponse(accessToken, "Bearer");
    }
}

package com.ringdu.server.auth.dto;

public record TokenRefreshResult(
        TokenRefreshResponse tokenRefreshResponse,
        String refreshToken
) {
}

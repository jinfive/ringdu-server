package com.ringdu.server.auth.dto;

public record RefreshTokenRotation(
        String refreshToken,
        Long userId
) {
}

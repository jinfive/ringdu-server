package com.ringdu.server.auth.dto;

public record LoginResult(
        LoginResponse loginResponse,
        String refreshToken
) {
}

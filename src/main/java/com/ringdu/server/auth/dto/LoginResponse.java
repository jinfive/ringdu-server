package com.ringdu.server.auth.dto;

import com.ringdu.server.user.entity.Role;
import com.ringdu.server.user.entity.User;

public record LoginResponse(
        String accessToken,
        String tokenType,
        Long userId,
        String email,
        String name,
        Role role
) {

    public static LoginResponse of(String accessToken, User user) {
        return new LoginResponse(
                accessToken,
                "Bearer",
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getRole()
        );
    }
}

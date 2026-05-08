package com.ringdu.server.auth.dto;

import com.ringdu.server.user.entity.AuthProvider;
import com.ringdu.server.user.entity.Role;
import com.ringdu.server.user.entity.User;

public record SignupResponse(
        Long userId,
        String email,
        String name,
        Role role,
        AuthProvider provider
) {

    public static SignupResponse from(User user) {
        return new SignupResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getRole(),
                user.getProvider()
        );
    }
}

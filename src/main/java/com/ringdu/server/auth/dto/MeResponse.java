package com.ringdu.server.auth.dto;

import com.ringdu.server.user.entity.AuthProvider;
import com.ringdu.server.user.entity.Role;
import com.ringdu.server.user.entity.User;
import com.ringdu.server.user.entity.UserStatus;

public record MeResponse(
        Long userId,
        String email,
        String name,
        String phone,
        Role role,
        UserStatus status,
        AuthProvider provider
) {

    public static MeResponse from(User user) {
        return new MeResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getPhone(),
                user.getRole(),
                user.getStatus(),
                user.getProvider()
        );
    }
}

package com.ringdu.server.global.security;

import com.ringdu.server.user.entity.Role;

public record CustomUserPrincipal(
        Long userId,
        String email,
        Role role
) {
}

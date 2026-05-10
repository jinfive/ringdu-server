package com.ringdu.server.admin.dto;

import com.ringdu.server.user.entity.Role;
import com.ringdu.server.user.entity.User;
import com.ringdu.server.user.entity.UserStatus;
import java.time.LocalDateTime;

public record AcademyAccountResponse(
        Long userId,
        String email,
        String name,
        String phone,
        Role role,
        UserStatus status,
        LocalDateTime createdAt
) {

    public static AcademyAccountResponse from(User user) {
        return new AcademyAccountResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getPhone(),
                user.getRole(),
                user.getStatus(),
                user.getCreatedAt()
        );
    }
}

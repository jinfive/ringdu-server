package com.ringdu.server.auth.dto;

import com.ringdu.server.academy.entity.AcademySignupApplication;
import com.ringdu.server.academy.entity.AcademySignupApplicationStatus;
import com.ringdu.server.user.entity.Role;
import com.ringdu.server.user.entity.User;
import com.ringdu.server.user.entity.UserStatus;
import java.time.LocalDateTime;

public record AcademySignupResponse(
        Long applicationId,
        Long userId,
        String email,
        String name,
        String phone,
        Role role,
        UserStatus status,
        String academyName,
        String representativeName,
        String postalCode,
        String address,
        String detailAddress,
        AcademySignupApplicationStatus applicationStatus,
        LocalDateTime createdAt
) {

    public static AcademySignupResponse from(AcademySignupApplication application) {
        User user = application.getUser();
        return new AcademySignupResponse(
                application.getId(),
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getPhone(),
                user.getRole(),
                user.getStatus(),
                application.getAcademyName(),
                application.getRepresentativeName(),
                application.getPostalCode(),
                application.getAddress(),
                application.getDetailAddress(),
                application.getStatus(),
                application.getCreatedAt()
        );
    }
}

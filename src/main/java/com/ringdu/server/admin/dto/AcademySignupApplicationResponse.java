package com.ringdu.server.admin.dto;

import com.ringdu.server.academy.entity.AcademySignupApplication;
import com.ringdu.server.academy.entity.AcademySignupApplicationStatus;
import java.time.LocalDateTime;

public record AcademySignupApplicationResponse(
        Long applicationId,
        Long userId,
        String email,
        String academyName,
        String representativeName,
        String phone,
        String postalCode,
        String address,
        String detailAddress,
        AcademySignupApplicationStatus status,
        LocalDateTime createdAt
) {

    public static AcademySignupApplicationResponse from(AcademySignupApplication application) {
        return new AcademySignupApplicationResponse(
                application.getId(),
                application.getUser().getId(),
                application.getUser().getEmail(),
                application.getAcademyName(),
                application.getRepresentativeName(),
                application.getPhone(),
                application.getPostalCode(),
                application.getAddress(),
                application.getDetailAddress(),
                application.getStatus(),
                application.getCreatedAt()
        );
    }
}

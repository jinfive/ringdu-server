package com.ringdu.server.admin.dto;

import com.ringdu.server.academy.entity.AcademySignupApplication;
import com.ringdu.server.academy.entity.AcademySignupApplicationStatus;
import com.ringdu.server.user.entity.Role;
import com.ringdu.server.user.entity.UserStatus;
import java.time.LocalDateTime;

public record AcademySignupApprovalResponse(
        Long applicationId,
        Long userId,
        String email,
        String academyName,
        Role role,
        UserStatus userStatus,
        AcademySignupApplicationStatus applicationStatus,
        LocalDateTime approvedAt
) {

    public static AcademySignupApprovalResponse from(AcademySignupApplication application) {
        return new AcademySignupApprovalResponse(
                application.getId(),
                application.getUser().getId(),
                application.getUser().getEmail(),
                application.getAcademyName(),
                application.getUser().getRole(),
                application.getUser().getStatus(),
                application.getStatus(),
                application.getReviewedAt()
        );
    }
}

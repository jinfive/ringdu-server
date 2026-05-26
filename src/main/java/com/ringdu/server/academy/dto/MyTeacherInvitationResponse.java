package com.ringdu.server.academy.dto;

import com.ringdu.server.academy.entity.AcademyTeacherInvitation;
import com.ringdu.server.academy.entity.AcademyTeacherInvitationStatus;
import java.time.LocalDateTime;

public record MyTeacherInvitationResponse(
        Long invitationId,
        Long academyId,
        String academyName,
        String teacherEmail,
        String message,
        AcademyTeacherInvitationStatus status,
        LocalDateTime createdAt,
        LocalDateTime expiresAt
) {

    public static MyTeacherInvitationResponse from(AcademyTeacherInvitation invitation) {
        return new MyTeacherInvitationResponse(
                invitation.getId(),
                invitation.getAcademy().getId(),
                invitation.getAcademy().getName(),
                invitation.getTeacherEmail(),
                invitation.getMessage(),
                invitation.getStatus(),
                invitation.getCreatedAt(),
                invitation.getExpiresAt()
        );
    }
}

package com.ringdu.server.academy.dto;

import com.ringdu.server.academy.entity.AcademyTeacherInvitation;
import com.ringdu.server.academy.entity.AcademyTeacherInvitationStatus;
import java.time.LocalDateTime;

public record TeacherInvitationResponse(
        Long invitationId,
        String teacherEmail,
        String teacherPhone,
        String message,
        AcademyTeacherInvitationStatus status,
        LocalDateTime createdAt,
        LocalDateTime respondedAt,
        LocalDateTime expiresAt
) {

    public static TeacherInvitationResponse from(AcademyTeacherInvitation invitation) {
        return new TeacherInvitationResponse(
                invitation.getId(),
                invitation.getTeacherEmail(),
                invitation.getTeacherPhone(),
                invitation.getMessage(),
                invitation.getStatus(),
                invitation.getCreatedAt(),
                invitation.getRespondedAt(),
                invitation.getExpiresAt()
        );
    }
}

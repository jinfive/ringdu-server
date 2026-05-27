package com.ringdu.server.academy.dto;

import com.ringdu.server.academy.entity.AcademyTeacherInvitation;
import com.ringdu.server.academy.entity.AcademyTeacherInvitationStatus;
import java.time.LocalDateTime;

public record MyTeacherInvitationResponse(
        Long invitationId,
        Long academyId,
        String academyName,
        Long teacherUserId,
        String teacherEmail,
        String teacherPhone,
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
                invitation.getTeacherUserId(),
                invitation.getTeacherEmail(),
                invitation.getTeacherPhone(),
                invitation.getMessage(),
                invitation.getStatus(),
                invitation.getCreatedAt(),
                invitation.getExpiresAt()
        );
    }
}

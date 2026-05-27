package com.ringdu.server.academy.dto;

import com.ringdu.server.academy.entity.AcademyStudentInvitationStatus;
import java.time.LocalDateTime;

public record AcademyStudentInvitationResponse(
        Long id,
        Long academyId,
        String academyName,
        Long studentProfileId,
        Long receiverUserId,
        String receiverEmail,
        String receiverPhone,
        String message,
        AcademyStudentInvitationStatus status,
        Long createdByUserId,
        Long respondedByUserId,
        LocalDateTime respondedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}

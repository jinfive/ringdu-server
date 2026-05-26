package com.ringdu.server.parentstudent.dto;

import com.ringdu.server.parentstudent.entity.ParentStudentInvitation;
import com.ringdu.server.parentstudent.entity.ParentStudentInvitationStatus;
import com.ringdu.server.user.entity.Role;
import java.time.LocalDateTime;

public record ParentStudentInvitationResponse(
        Long invitationId,
        Long requesterUserId,
        String requesterName,
        String receiverEmail,
        String receiverPhone,
        Role requesterRole,
        Role targetRole,
        Long studentUserId,
        Long parentUserId,
        String message,
        ParentStudentInvitationStatus status,
        String direction,
        LocalDateTime createdAt,
        LocalDateTime respondedAt,
        LocalDateTime expiresAt
) {

    public static ParentStudentInvitationResponse from(
            ParentStudentInvitation invitation,
            String requesterName,
            Long currentUserId
    ) {
        String direction = invitation.getRequesterUserId().equals(currentUserId) ? "SENT" : "RECEIVED";
        return new ParentStudentInvitationResponse(
                invitation.getId(),
                invitation.getRequesterUserId(),
                requesterName,
                invitation.getReceiverEmail(),
                invitation.getReceiverPhone(),
                invitation.getRequesterRole(),
                invitation.getTargetRole(),
                invitation.getStudentUserId(),
                invitation.getParentUserId(),
                invitation.getMessage(),
                invitation.getStatus(),
                direction,
                invitation.getCreatedAt(),
                invitation.getRespondedAt(),
                invitation.getExpiresAt()
        );
    }
}

package com.ringdu.server.parentstudent.entity;

import com.ringdu.server.global.common.BaseEntity;
import com.ringdu.server.global.exception.BusinessException;
import com.ringdu.server.global.exception.ErrorCode;
import com.ringdu.server.user.entity.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "parent_student_invitations",
        indexes = {
                @Index(name = "idx_parent_student_invitations_requester_user_id", columnList = "requester_user_id"),
                @Index(name = "idx_parent_student_invitations_receiver_email", columnList = "receiver_email"),
                @Index(name = "idx_parent_student_invitations_receiver_phone", columnList = "receiver_phone"),
                @Index(name = "idx_parent_student_invitations_status", columnList = "status"),
                @Index(name = "idx_parent_student_invitations_parent_user_id", columnList = "parent_user_id"),
                @Index(name = "idx_parent_student_invitations_student_user_id", columnList = "student_user_id")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ParentStudentInvitation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long requesterUserId;

    @Column(length = 100)
    private String receiverEmail;

    @Column(nullable = false, length = 30)
    private String receiverPhone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role requesterRole;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role targetRole;

    private Long studentUserId;

    private Long parentUserId;

    @Column(length = 500)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ParentStudentInvitationStatus status;

    private Long respondedByUserId;

    private LocalDateTime respondedAt;

    private LocalDateTime expiresAt;

    private ParentStudentInvitation(
            Long requesterUserId,
            String receiverEmail,
            String receiverPhone,
            Role requesterRole,
            Role targetRole,
            Long studentUserId,
            Long parentUserId,
            String message,
            LocalDateTime expiresAt
    ) {
        this.requesterUserId = requesterUserId;
        this.receiverEmail = receiverEmail;
        this.receiverPhone = receiverPhone;
        this.requesterRole = requesterRole;
        this.targetRole = targetRole;
        this.studentUserId = studentUserId;
        this.parentUserId = parentUserId;
        this.message = message;
        this.expiresAt = expiresAt;
        this.status = ParentStudentInvitationStatus.PENDING;
    }

    public static ParentStudentInvitation create(
            Long requesterUserId,
            String receiverEmail,
            String receiverPhone,
            Role requesterRole,
            Role targetRole,
            Long studentUserId,
            Long parentUserId,
            String message,
            LocalDateTime expiresAt
    ) {
        return new ParentStudentInvitation(
                requesterUserId,
                receiverEmail,
                receiverPhone,
                requesterRole,
                targetRole,
                studentUserId,
                parentUserId,
                message,
                expiresAt
        );
    }

    public void accept(Long receiverUserId, Long parentUserId, Long studentUserId, LocalDateTime respondedAt) {
        validatePending();
        this.status = ParentStudentInvitationStatus.ACCEPTED;
        this.respondedByUserId = receiverUserId;
        this.respondedAt = respondedAt;
        this.parentUserId = parentUserId;
        this.studentUserId = studentUserId;
    }

    public void reject(Long receiverUserId, LocalDateTime respondedAt) {
        validatePending();
        this.status = ParentStudentInvitationStatus.REJECTED;
        this.respondedByUserId = receiverUserId;
        this.respondedAt = respondedAt;
    }

    public boolean isPending() {
        return status == ParentStudentInvitationStatus.PENDING;
    }

    private void validatePending() {
        if (!isPending()) {
            throw new BusinessException(ErrorCode.PARENT_STUDENT_INVITATION_ALREADY_PROCESSED);
        }
    }
}

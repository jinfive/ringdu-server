package com.ringdu.server.academy.entity;

import com.ringdu.server.global.common.BaseEntity;
import com.ringdu.server.global.exception.BusinessException;
import com.ringdu.server.global.exception.ErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "academy_teacher_invitations",
        indexes = {
                @Index(name = "idx_teacher_invitations_academy_id", columnList = "academy_id"),
                @Index(name = "idx_teacher_invitations_teacher_phone", columnList = "teacher_phone"),
                @Index(name = "idx_teacher_invitations_teacher_user_id", columnList = "teacher_user_id"),
                @Index(name = "idx_teacher_invitations_status", columnList = "status")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AcademyTeacherInvitation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "academy_id", nullable = false)
    private Academy academy;

    @Column(name = "teacher_user_id")
    private Long teacherUserId;

    @Column(name = "teacher_email", length = 100)
    private String teacherEmail;

    @Column(name = "teacher_phone", nullable = false, length = 30)
    private String teacherPhone;

    @Column(length = 500)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AcademyTeacherInvitationStatus status;

    @Column(nullable = false)
    private Long invitedByUserId;

    private Long respondedByUserId;

    private LocalDateTime respondedAt;

    private LocalDateTime expiresAt;

    private AcademyTeacherInvitation(
            Academy academy,
            Long teacherUserId,
            String teacherEmail,
            String teacherPhone,
            String message,
            Long invitedByUserId,
            LocalDateTime expiresAt
    ) {
        this.academy = academy;
        this.teacherUserId = teacherUserId;
        this.teacherEmail = teacherEmail;
        this.teacherPhone = teacherPhone;
        this.message = message;
        this.invitedByUserId = invitedByUserId;
        this.expiresAt = expiresAt;
        this.status = AcademyTeacherInvitationStatus.PENDING;
    }

    public static AcademyTeacherInvitation create(
            Academy academy,
            Long teacherUserId,
            String teacherEmail,
            String teacherPhone,
            String message,
            Long invitedByUserId,
            LocalDateTime expiresAt
    ) {
        return new AcademyTeacherInvitation(
                academy,
                teacherUserId,
                teacherEmail,
                teacherPhone,
                message,
                invitedByUserId,
                expiresAt
        );
    }

    public void accept(Long teacherUserId, LocalDateTime respondedAt) {
        validatePending();
        if (this.teacherUserId == null) {
            this.teacherUserId = teacherUserId;
        }
        this.status = AcademyTeacherInvitationStatus.ACCEPTED;
        this.respondedByUserId = teacherUserId;
        this.respondedAt = respondedAt;
    }

    public void reject(Long teacherUserId, LocalDateTime respondedAt) {
        validatePending();
        this.status = AcademyTeacherInvitationStatus.REJECTED;
        this.respondedByUserId = teacherUserId;
        this.respondedAt = respondedAt;
    }

    public boolean isPending() {
        return status == AcademyTeacherInvitationStatus.PENDING;
    }

    private void validatePending() {
        if (!isPending()) {
            throw new BusinessException(ErrorCode.TEACHER_INVITATION_ALREADY_PROCESSED);
        }
    }
}

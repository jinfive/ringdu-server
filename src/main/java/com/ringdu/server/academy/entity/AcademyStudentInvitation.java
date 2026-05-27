package com.ringdu.server.academy.entity;

import com.ringdu.server.global.common.BaseEntity;
import com.ringdu.server.global.exception.BusinessException;
import com.ringdu.server.global.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(
        name = "academy_student_invitations",
        indexes = {
                @Index(name = "idx_academy_student_invitations_academy_id", columnList = "academy_id"),
                @Index(name = "idx_academy_student_invitations_receiver_user_id", columnList = "receiver_user_id"),
                @Index(name = "idx_academy_student_invitations_status", columnList = "status"),
                @Index(name = "idx_academy_student_invitations_academy_receiver_status", columnList = "academy_id, receiver_user_id, status")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AcademyStudentInvitation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "academy_id", nullable = false)
    private Academy academy;

    private Long studentProfileId;

    @Column(nullable = false)
    private Long receiverUserId;

    private String receiverEmail;

    private String receiverPhone;

    @Column(length = 500)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AcademyStudentInvitationStatus status;

    @Column(nullable = false)
    private Long createdByUserId;

    private Long respondedByUserId;

    private LocalDateTime respondedAt;

    @Builder
    public AcademyStudentInvitation(Academy academy, Long studentProfileId, Long receiverUserId,
                                   String receiverEmail, String receiverPhone, String message,
                                   Long createdByUserId) {
        this.academy = academy;
        this.studentProfileId = studentProfileId;
        this.receiverUserId = receiverUserId;
        this.receiverEmail = receiverEmail;
        this.receiverPhone = receiverPhone;
        this.message = message;
        this.status = AcademyStudentInvitationStatus.PENDING;
        this.createdByUserId = createdByUserId;
    }

    public void accept(Long userId) {
        validatePending();
        this.status = AcademyStudentInvitationStatus.ACCEPTED;
        this.respondedByUserId = userId;
        this.respondedAt = LocalDateTime.now();
    }

    public void reject(Long userId) {
        validatePending();
        this.status = AcademyStudentInvitationStatus.REJECTED;
        this.respondedByUserId = userId;
        this.respondedAt = LocalDateTime.now();
    }

    public boolean isPending() {
        return status == AcademyStudentInvitationStatus.PENDING;
    }

    private void validatePending() {
        if (!isPending()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "이미 처리된 초대장입니다.");
        }
    }
}

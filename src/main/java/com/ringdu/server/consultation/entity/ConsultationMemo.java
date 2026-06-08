package com.ringdu.server.consultation.entity;

import com.ringdu.server.global.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "consultation_memos",
        indexes = {
                @Index(name = "idx_consultation_memos_academy_student", columnList = "academy_id, student_profile_id"),
                @Index(name = "idx_consultation_memos_request", columnList = "consultation_request_id"),
                @Index(name = "idx_consultation_memos_writer", columnList = "writer_user_id, writer_role"),
                @Index(name = "idx_consultation_memos_date", columnList = "consultation_date")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ConsultationMemo extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "academy_id", nullable = false)
    private Long academyId;

    @Column(name = "student_profile_id", nullable = false)
    private Long studentProfileId;

    @Column(name = "consultation_request_id")
    private Long consultationRequestId;

    @Column(name = "writer_user_id", nullable = false)
    private Long writerUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "writer_role", nullable = false, length = 20)
    private ConsultationMemoWriterRole writerRole;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "next_action", columnDefinition = "TEXT")
    private String nextAction;

    @Column(name = "consultation_date", nullable = false)
    private LocalDate consultationDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ConsultationMemoStatus status;

    private ConsultationMemo(
            Long academyId,
            Long studentProfileId,
            Long consultationRequestId,
            Long writerUserId,
            ConsultationMemoWriterRole writerRole,
            String title,
            String content,
            String nextAction,
            LocalDate consultationDate
    ) {
        this.academyId = academyId;
        this.studentProfileId = studentProfileId;
        this.consultationRequestId = consultationRequestId;
        this.writerUserId = writerUserId;
        this.writerRole = writerRole;
        this.title = title;
        this.content = content;
        this.nextAction = nextAction;
        this.consultationDate = consultationDate;
        this.status = ConsultationMemoStatus.ACTIVE;
    }

    public static ConsultationMemo create(
            Long academyId,
            Long studentProfileId,
            Long consultationRequestId,
            Long writerUserId,
            ConsultationMemoWriterRole writerRole,
            String title,
            String content,
            String nextAction,
            LocalDate consultationDate
    ) {
        return new ConsultationMemo(
                academyId,
                studentProfileId,
                consultationRequestId,
                writerUserId,
                writerRole,
                title,
                content,
                nextAction,
                consultationDate
        );
    }

    public void update(String title, String content, String nextAction, LocalDate consultationDate) {
        this.title = title;
        this.content = content;
        this.nextAction = nextAction;
        this.consultationDate = consultationDate;
    }
}

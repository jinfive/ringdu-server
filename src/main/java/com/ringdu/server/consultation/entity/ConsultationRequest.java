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
import java.time.LocalTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "consultation_requests",
        indexes = {
                @Index(name = "idx_consultation_requests_academy_status", columnList = "academy_id, status"),
                @Index(name = "idx_consultation_requests_parent_user_id", columnList = "parent_user_id"),
                @Index(name = "idx_consultation_requests_student_profile_id", columnList = "student_profile_id"),
                @Index(name = "idx_consultation_requests_requested_date", columnList = "requested_date")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ConsultationRequest extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "academy_id", nullable = false)
    private Long academyId;

    @Column(name = "student_profile_id", nullable = false)
    private Long studentProfileId;

    @Column(name = "parent_user_id", nullable = false)
    private Long parentUserId;

    @Column(name = "teacher_user_id")
    private Long teacherUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "consultation_type", nullable = false, length = 30)
    private ConsultationRequestType consultationType;

    @Column(name = "requested_date", nullable = false)
    private LocalDate requestedDate;

    @Column(name = "requested_start_time", nullable = false)
    private LocalTime requestedStartTime;

    @Column(name = "requested_end_time", nullable = false)
    private LocalTime requestedEndTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ConsultationTopic topic;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ConsultationRequestStatus status;

    @Column(name = "academy_memo", columnDefinition = "TEXT")
    private String academyMemo;

    private ConsultationRequest(
            Long academyId,
            Long studentProfileId,
            Long parentUserId,
            Long teacherUserId,
            LocalDate requestedDate,
            LocalTime requestedStartTime,
            LocalTime requestedEndTime,
            ConsultationTopic topic,
            String content
    ) {
        this.academyId = academyId;
        this.studentProfileId = studentProfileId;
        this.parentUserId = parentUserId;
        this.teacherUserId = teacherUserId;
        this.consultationType = ConsultationRequestType.ENROLLED_STUDENT;
        this.requestedDate = requestedDate;
        this.requestedStartTime = requestedStartTime;
        this.requestedEndTime = requestedEndTime;
        this.topic = topic;
        this.content = content;
        this.status = ConsultationRequestStatus.REQUESTED;
    }

    public static ConsultationRequest create(
            Long academyId,
            Long studentProfileId,
            Long parentUserId,
            Long teacherUserId,
            LocalDate requestedDate,
            LocalTime requestedStartTime,
            LocalTime requestedEndTime,
            ConsultationTopic topic,
            String content
    ) {
        return new ConsultationRequest(
                academyId,
                studentProfileId,
                parentUserId,
                teacherUserId,
                requestedDate,
                requestedStartTime,
                requestedEndTime,
                topic,
                content
        );
    }

    public void approve(String memo) {
        this.status = ConsultationRequestStatus.APPROVED;
        this.academyMemo = memo;
    }

    public void reject(String memo) {
        this.status = ConsultationRequestStatus.REJECTED;
        this.academyMemo = memo;
    }

    public void complete(String memo) {
        this.status = ConsultationRequestStatus.COMPLETED;
        this.academyMemo = memo;
    }

    public void cancel() {
        this.status = ConsultationRequestStatus.CANCELED;
    }

    public ConsultationConsultantType getConsultantType() {
        return teacherUserId == null
                ? ConsultationConsultantType.ACADEMY_ACCOUNT
                : ConsultationConsultantType.TEACHER;
    }
}

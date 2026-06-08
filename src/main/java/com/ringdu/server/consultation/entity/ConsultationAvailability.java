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
import java.time.DayOfWeek;
import java.time.LocalTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "consultation_availabilities",
        indexes = {
                @Index(
                        name = "idx_consultation_availabilities_teacher_day",
                        columnList = "academy_id, teacher_user_id, day_of_week"
                ),
                @Index(name = "idx_consultation_availabilities_status", columnList = "status")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ConsultationAvailability extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "academy_id", nullable = false)
    private Long academyId;

    @Column(name = "teacher_user_id")
    private Long teacherUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false, length = 20)
    private DayOfWeek dayOfWeek;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "consultation_type", nullable = false, length = 30)
    private ConsultationType consultationType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ConsultationAvailabilityStatus status;

    private ConsultationAvailability(
            Long academyId,
            Long teacherUserId,
            DayOfWeek dayOfWeek,
            LocalTime startTime,
            LocalTime endTime,
            ConsultationType consultationType
    ) {
        this.academyId = academyId;
        this.teacherUserId = teacherUserId;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.consultationType = consultationType;
        this.status = ConsultationAvailabilityStatus.ACTIVE;
    }

    public static ConsultationAvailability create(
            Long academyId,
            Long teacherUserId,
            DayOfWeek dayOfWeek,
            LocalTime startTime,
            LocalTime endTime,
            ConsultationType consultationType
    ) {
        return new ConsultationAvailability(academyId, teacherUserId, dayOfWeek, startTime, endTime, consultationType);
    }

    public void update(DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime, ConsultationType consultationType) {
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.consultationType = consultationType;
        this.status = ConsultationAvailabilityStatus.ACTIVE;
    }

    public void deactivate() {
        this.status = ConsultationAvailabilityStatus.INACTIVE;
    }
}

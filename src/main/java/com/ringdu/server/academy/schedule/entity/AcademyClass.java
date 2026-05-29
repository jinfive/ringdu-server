package com.ringdu.server.academy.schedule.entity;

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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Getter
@Entity
@Table(
        name = "academy_classes",
        indexes = {
                @Index(name = "idx_academy_classes_academy_day", columnList = "academy_id, day_of_week"),
                @Index(name = "idx_academy_classes_classroom_day_time", columnList = "academy_id, classroom_id, day_of_week, start_time, end_time"),
                @Index(name = "idx_academy_classes_status", columnList = "status")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AcademyClass extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "academy_id", nullable = false)
    private Long academyId;

    @Column(name = "classroom_id", nullable = false)
    private Long classroomId;

    @Column(name = "teacher_user_id")
    private Long teacherUserId;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false, length = 20)
    private AcademyClassDayOfWeek dayOfWeek;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(columnDefinition = "TEXT")
    private String memo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ScheduleStatus status;

    private AcademyClass(
            Long academyId,
            Long classroomId,
            Long teacherUserId,
            String name,
            AcademyClassDayOfWeek dayOfWeek,
            LocalTime startTime,
            LocalTime endTime,
            String memo
    ) {
        this.academyId = academyId;
        this.classroomId = classroomId;
        this.teacherUserId = teacherUserId;
        this.name = name;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.memo = memo;
        this.status = ScheduleStatus.ACTIVE;
    }

    public static AcademyClass create(
            Long academyId,
            Long classroomId,
            Long teacherUserId,
            String name,
            AcademyClassDayOfWeek dayOfWeek,
            LocalTime startTime,
            LocalTime endTime,
            String memo
    ) {
        return new AcademyClass(academyId, classroomId, teacherUserId, name, dayOfWeek, startTime, endTime, memo);
    }

    public void update(
            Long classroomId,
            Long teacherUserId,
            String name,
            AcademyClassDayOfWeek dayOfWeek,
            LocalTime startTime,
            LocalTime endTime,
            String memo
    ) {
        this.classroomId = classroomId;
        this.teacherUserId = teacherUserId;
        this.name = name;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.memo = memo;
    }

    public void deactivate() {
        this.status = ScheduleStatus.INACTIVE;
    }
}

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
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "academy_class_students",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_academy_class_students_class_student", columnNames = {"academy_class_id", "student_profile_id"})
        },
        indexes = {
                @Index(name = "idx_academy_class_students_class_id", columnList = "academy_class_id"),
                @Index(name = "idx_academy_class_students_student_id", columnList = "student_profile_id"),
                @Index(name = "idx_academy_class_students_status", columnList = "status")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AcademyClassStudent extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "academy_class_id", nullable = false)
    private Long academyClassId;

    @Column(name = "student_profile_id", nullable = false)
    private Long studentProfileId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ScheduleStatus status;

    private AcademyClassStudent(Long academyClassId, Long studentProfileId) {
        this.academyClassId = academyClassId;
        this.studentProfileId = studentProfileId;
        this.status = ScheduleStatus.ACTIVE;
    }

    public static AcademyClassStudent create(Long academyClassId, Long studentProfileId) {
        return new AcademyClassStudent(academyClassId, studentProfileId);
    }

    public void activate() {
        this.status = ScheduleStatus.ACTIVE;
    }

    public void deactivate() {
        this.status = ScheduleStatus.INACTIVE;
    }
}

package com.ringdu.server.homework.entity;

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
        name = "homework_students",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_homework_students_homework_student", columnNames = {"homework_id", "student_profile_id"})
        },
        indexes = {
                @Index(name = "idx_homework_students_homework_id", columnList = "homework_id"),
                @Index(name = "idx_homework_students_student_id", columnList = "student_profile_id"),
                @Index(name = "idx_homework_students_status", columnList = "status")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HomeworkStudent extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "homework_id", nullable = false)
    private Long homeworkId;

    @Column(name = "student_profile_id", nullable = false)
    private Long studentProfileId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private HomeworkStudentStatus status;

    @Column(columnDefinition = "TEXT")
    private String memo;

    private HomeworkStudent(Long homeworkId, Long studentProfileId) {
        this.homeworkId = homeworkId;
        this.studentProfileId = studentProfileId;
        this.status = HomeworkStudentStatus.NOT_DONE;
    }

    public static HomeworkStudent create(Long homeworkId, Long studentProfileId) {
        return new HomeworkStudent(homeworkId, studentProfileId);
    }

    public void update(HomeworkStudentStatus status, String memo) {
        this.status = status;
        this.memo = memo;
    }
}

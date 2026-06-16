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
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "homeworks",
        indexes = {
                @Index(name = "idx_homeworks_academy_id", columnList = "academy_id"),
                @Index(name = "idx_homeworks_class_status", columnList = "class_id, status"),
                @Index(name = "idx_homeworks_due_date", columnList = "due_date")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Homework extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "academy_id", nullable = false)
    private Long academyId;

    @Column(name = "class_id", nullable = false)
    private Long classId;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 20)
    private HomeworkTargetType targetType;

    @Column(columnDefinition = "TEXT")
    private String memo;

    @Column(name = "created_by_teacher_user_id", nullable = false)
    private Long createdByTeacherUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private HomeworkStatus status;

    private Homework(
            Long academyId,
            Long classId,
            String title,
            String content,
            LocalDate dueDate,
            HomeworkTargetType targetType,
            String memo,
            Long createdByTeacherUserId
    ) {
        this.academyId = academyId;
        this.classId = classId;
        this.title = title;
        this.content = content;
        this.dueDate = dueDate;
        this.targetType = targetType;
        this.memo = memo;
        this.createdByTeacherUserId = createdByTeacherUserId;
        this.status = HomeworkStatus.ACTIVE;
    }

    public static Homework create(
            Long academyId,
            Long classId,
            String title,
            String content,
            LocalDate dueDate,
            HomeworkTargetType targetType,
            String memo,
            Long createdByTeacherUserId
    ) {
        return new Homework(academyId, classId, title, content, dueDate, targetType, memo, createdByTeacherUserId);
    }

    public void delete() {
        this.status = HomeworkStatus.DELETED;
    }
}

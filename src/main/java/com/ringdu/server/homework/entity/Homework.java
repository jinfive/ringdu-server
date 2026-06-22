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
                @Index(name = "idx_homeworks_class_id", columnList = "academy_class_id"),
                @Index(name = "idx_homeworks_due_date", columnList = "due_date"),
                @Index(name = "idx_homeworks_status", columnList = "status")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Homework extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "academy_id", nullable = false)
    private Long academyId;

    @Column(name = "academy_class_id", nullable = false)
    private Long academyClassId;

    @Column(nullable = false, length = 120)
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private HomeworkStatus status;

    private Homework(Long academyId, Long academyClassId, String title, String content,
                     LocalDate dueDate, HomeworkTargetType targetType, String memo) {
        this.academyId = academyId;
        this.academyClassId = academyClassId;
        this.title = title;
        this.content = content;
        this.dueDate = dueDate;
        this.targetType = targetType;
        this.memo = memo;
        this.status = HomeworkStatus.ACTIVE;
    }

    public static Homework create(Long academyId, Long academyClassId, String title, String content,
                                  LocalDate dueDate, HomeworkTargetType targetType, String memo) {
        return new Homework(academyId, academyClassId, title, content, dueDate, targetType, memo);
    }

    public void delete() {
        this.status = HomeworkStatus.DELETED;
    }
}

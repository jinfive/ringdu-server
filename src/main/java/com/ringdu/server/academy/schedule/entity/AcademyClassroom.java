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
        name = "academy_classrooms",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_academy_classrooms_academy_name", columnNames = {"academy_id", "name"})
        },
        indexes = {
                @Index(name = "idx_academy_classrooms_academy_display_order", columnList = "academy_id, display_order"),
                @Index(name = "idx_academy_classrooms_academy_status", columnList = "academy_id, status")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AcademyClassroom extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "academy_id", nullable = false)
    private Long academyId;

    @Column(nullable = false, length = 50)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ScheduleStatus status;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    private AcademyClassroom(Long academyId, String name, int displayOrder) {
        this.academyId = academyId;
        this.name = name;
        this.displayOrder = displayOrder;
        this.status = ScheduleStatus.ACTIVE;
    }

    public static AcademyClassroom create(Long academyId, String name, int displayOrder) {
        return new AcademyClassroom(academyId, name, displayOrder);
    }

    public void updateName(String name) {
        this.name = name;
    }

    public void deactivate() {
        this.status = ScheduleStatus.INACTIVE;
    }
}

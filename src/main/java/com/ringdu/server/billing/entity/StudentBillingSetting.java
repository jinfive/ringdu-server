package com.ringdu.server.billing.entity;

import com.ringdu.server.global.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Check;

@Getter
@Entity
@Table(
        name = "student_billing_settings",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_student_billing_settings_academy_student",
                columnNames = {"academy_id", "student_profile_id"}
        ),
        indexes = @Index(
                name = "idx_student_billing_settings_academy_student",
                columnList = "academy_id, student_profile_id"
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Check(constraints = "monthly_tuition >= 0 AND due_day BETWEEN 1 AND 28")
public class StudentBillingSetting extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "academy_id", nullable = false)
    private Long academyId;

    @Column(name = "student_profile_id", nullable = false)
    private Long studentProfileId;

    @Column(name = "monthly_tuition", nullable = false)
    private Long monthlyTuition;

    @Column(name = "due_day", nullable = false)
    private Integer dueDay;

    @Column(columnDefinition = "TEXT")
    private String memo;

    public StudentBillingSetting(Long academyId, Long studentProfileId, Long monthlyTuition, Integer dueDay, String memo) {
        this.academyId = academyId;
        this.studentProfileId = studentProfileId;
        this.monthlyTuition = monthlyTuition;
        this.dueDay = dueDay;
        this.memo = memo;
    }

    public void update(Long monthlyTuition, Integer dueDay, String memo) {
        this.monthlyTuition = monthlyTuition;
        this.dueDay = dueDay;
        this.memo = memo;
    }
}

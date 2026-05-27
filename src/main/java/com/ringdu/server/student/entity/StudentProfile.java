package com.ringdu.server.student.entity;

import com.ringdu.server.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(
        name = "student_profiles",
        indexes = {
                @Index(name = "idx_student_profiles_academy_id", columnList = "academyId"),
                @Index(name = "idx_student_profiles_academy_status", columnList = "academyId, status"),
                @Index(name = "idx_student_profiles_email", columnList = "email"),
                @Index(name = "idx_student_profiles_phone", columnList = "phone"),
                @Index(name = "idx_student_profiles_user_id", columnList = "userId")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StudentProfile extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long academyId;

    private Long userId;

    @Column(nullable = false)
    private String name;

    private LocalDate birthDate;

    private String school;

    private String grade;

    private String email;

    private String phone;

    private String guardianPhone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StudentStatus status;

    @Column(columnDefinition = "TEXT")
    private String memo;

    @Builder
    public StudentProfile(Long academyId, Long userId, String name, LocalDate birthDate,
                          String school, String grade, String email, String phone,
                          String guardianPhone, StudentStatus status, String memo) {
        this.academyId = academyId;
        this.userId = userId;
        this.name = name;
        this.birthDate = birthDate;
        this.school = school;
        this.grade = grade;
        this.email = email;
        this.phone = phone;
        this.guardianPhone = guardianPhone;
        this.status = status;
        this.memo = memo;
    }

    public void update(String name, LocalDate birthDate, String school, String grade,
                       String email, String phone, String guardianPhone,
                       StudentStatus status, String memo) {
        this.name = name;
        this.birthDate = birthDate;
        this.school = school;
        this.grade = grade;
        this.email = email;
        this.phone = phone;
        this.guardianPhone = guardianPhone;
        this.status = status;
        this.memo = memo;
    }

    public void updateUserId(Long userId) {
        this.userId = userId;
    }
}

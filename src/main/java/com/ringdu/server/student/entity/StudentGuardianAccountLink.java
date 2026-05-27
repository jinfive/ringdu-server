package com.ringdu.server.student.entity;

import com.ringdu.server.global.common.BaseEntity;
import com.ringdu.server.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "student_guardian_account_links",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_student_guardian_account_links_profile_parent",
                        columnNames = {"student_profile_id", "parent_user_id"}
                )
        },
        indexes = {
                @Index(name = "idx_student_guardian_account_links_profile", columnList = "student_profile_id"),
                @Index(name = "idx_student_guardian_account_links_parent", columnList = "parent_user_id")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StudentGuardianAccountLink extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_profile_id", nullable = false)
    private Long studentProfileId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "parent_user_id", nullable = false)
    private User parent;

    private StudentGuardianAccountLink(Long studentProfileId, User parent) {
        this.studentProfileId = studentProfileId;
        this.parent = parent;
    }

    public static StudentGuardianAccountLink create(Long studentProfileId, User parent) {
        return new StudentGuardianAccountLink(studentProfileId, parent);
    }
}

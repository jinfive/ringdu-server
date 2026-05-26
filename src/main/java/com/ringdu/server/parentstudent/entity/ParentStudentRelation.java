package com.ringdu.server.parentstudent.entity;

import com.ringdu.server.global.common.BaseEntity;
import com.ringdu.server.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
        name = "parent_student_relations",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_parent_student_relations_parent_student",
                        columnNames = {"parent_user_id", "student_user_id"}
                )
        },
        indexes = {
                @Index(name = "idx_parent_student_relations_parent_user_id", columnList = "parent_user_id"),
                @Index(name = "idx_parent_student_relations_student_user_id", columnList = "student_user_id"),
                @Index(name = "idx_parent_student_relations_status", columnList = "status")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ParentStudentRelation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "parent_user_id", nullable = false)
    private User parent;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_user_id", nullable = false)
    private User student;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ParentStudentRelationStatus status;

    private ParentStudentRelation(User parent, User student) {
        this.parent = parent;
        this.student = student;
        this.status = ParentStudentRelationStatus.ACTIVE;
    }

    public static ParentStudentRelation create(User parent, User student) {
        return new ParentStudentRelation(parent, student);
    }
}

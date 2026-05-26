package com.ringdu.server.academy.entity;

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
        name = "academy_members",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_academy_members_academy_user", columnNames = {"academy_id", "user_id"})
        },
        indexes = {
                @Index(name = "idx_academy_members_academy_id", columnList = "academy_id"),
                @Index(name = "idx_academy_members_user_id", columnList = "user_id"),
                @Index(name = "idx_academy_members_status", columnList = "status")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AcademyMember extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "academy_id", nullable = false)
    private Academy academy;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AcademyMemberRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AcademyMemberStatus status;

    private AcademyMember(Academy academy, User user, AcademyMemberRole role) {
        this.academy = academy;
        this.user = user;
        this.role = role;
        this.status = AcademyMemberStatus.ACTIVE;
    }

    public static AcademyMember createTeacher(Academy academy, User user) {
        return new AcademyMember(academy, user, AcademyMemberRole.TEACHER);
    }
}

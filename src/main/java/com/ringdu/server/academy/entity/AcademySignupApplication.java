package com.ringdu.server.academy.entity;

import com.ringdu.server.global.common.BaseEntity;
import com.ringdu.server.global.exception.BusinessException;
import com.ringdu.server.global.exception.ErrorCode;
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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "academy_signup_applications",
        indexes = {
                @Index(name = "idx_academy_signup_applications_status", columnList = "status"),
                @Index(name = "idx_academy_signup_applications_created_at", columnList = "created_at")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AcademySignupApplication extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false, length = 100)
    private String academyName;

    @Column(nullable = false, length = 50)
    private String representativeName;

    @Column(nullable = false, length = 30)
    private String phone;

    @Column(nullable = false, length = 20)
    private String postalCode;

    @Column(nullable = false, length = 255)
    private String address;

    @Column(nullable = false, length = 255)
    private String detailAddress;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AcademySignupApplicationStatus status;

    private Long reviewedBy;

    private LocalDateTime reviewedAt;

    private AcademySignupApplication(
            User user,
            String academyName,
            String representativeName,
            String phone,
            String postalCode,
            String address,
            String detailAddress
    ) {
        this.user = user;
        this.academyName = academyName;
        this.representativeName = representativeName;
        this.phone = phone;
        this.postalCode = postalCode;
        this.address = address;
        this.detailAddress = detailAddress;
        this.status = AcademySignupApplicationStatus.PENDING;
    }

    public static AcademySignupApplication create(
            User user,
            String academyName,
            String representativeName,
            String phone,
            String postalCode,
            String address,
            String detailAddress
    ) {
        return new AcademySignupApplication(
                user,
                academyName,
                representativeName,
                phone,
                postalCode,
                address,
                detailAddress
        );
    }

    public void approve(Long adminUserId, LocalDateTime reviewedAt) {
        validatePending();
        this.status = AcademySignupApplicationStatus.APPROVED;
        this.reviewedBy = adminUserId;
        this.reviewedAt = reviewedAt;
        this.user.activate();
    }

    private void validatePending() {
        if (status != AcademySignupApplicationStatus.PENDING) {
            throw new BusinessException(ErrorCode.ACADEMY_SIGNUP_APPLICATION_ALREADY_REVIEWED);
        }
    }
}

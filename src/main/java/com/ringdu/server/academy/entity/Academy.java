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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "academies",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_academies_user_id", columnNames = "user_id")
        },
        indexes = {
                @Index(name = "idx_academies_status", columnList = "status")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Academy extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 50)
    private String representativeName;

    @Column(nullable = false, length = 30)
    private String phone;

    @Column(length = 20)
    private String postalCode;

    @Column(length = 255)
    private String address;

    @Column(length = 255)
    private String detailAddress;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AcademyStatus status;

    private Academy(
            User user,
            String name,
            String representativeName,
            String phone,
            String postalCode,
            String address,
            String detailAddress
    ) {
        this.user = user;
        this.name = name;
        this.representativeName = representativeName;
        this.phone = phone;
        this.postalCode = postalCode;
        this.address = address;
        this.detailAddress = detailAddress;
        this.status = AcademyStatus.ACTIVE;
    }

    public static Academy create(
            User user,
            String name,
            String representativeName,
            String phone,
            String postalCode,
            String address,
            String detailAddress
    ) {
        return new Academy(user, name, representativeName, phone, postalCode, address, detailAddress);
    }

    public void update(
            String name,
            String representativeName,
            String phone,
            String postalCode,
            String address,
            String detailAddress
    ) {
        this.name = name;
        this.representativeName = representativeName;
        this.phone = phone;
        this.postalCode = postalCode;
        this.address = address;
        this.detailAddress = detailAddress;
    }
}

package com.ringdu.server.user.entity;

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
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_users_email", columnNames = "email")
        },
        indexes = {
                @Index(name = "idx_users_provider_provider_id", columnList = "provider, provider_id")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(length = 100)
    private String password;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 30)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AuthProvider provider;

    @Column(name = "provider_id", length = 100)
    private String providerId;

    private User(String email, String password, String name, String phone, Role role,
                 UserStatus status, AuthProvider provider, String providerId) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.phone = phone;
        this.role = role;
        this.status = status;
        this.provider = provider;
        this.providerId = providerId;
    }

    public static User createLocalUser(String email, String encodedPassword, String name, String phone, Role role) {
        return new User(
                email,
                encodedPassword,
                name,
                phone,
                role,
                UserStatus.ACTIVE,
                AuthProvider.LOCAL,
                null
        );
    }

    public static User createSocialUser(String email, String name, String phone, Role role,
                                        AuthProvider provider, String providerId) {
        return new User(
                email,
                null,
                name,
                phone,
                role,
                UserStatus.ACTIVE,
                provider,
                providerId
        );
    }

    public void deactivate() {
        this.status = UserStatus.INACTIVE;
    }
}

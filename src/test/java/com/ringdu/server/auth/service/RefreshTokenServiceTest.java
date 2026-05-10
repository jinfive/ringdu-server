package com.ringdu.server.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ringdu.server.auth.repository.InMemoryRefreshTokenStore;
import com.ringdu.server.global.exception.BusinessException;
import com.ringdu.server.global.exception.ErrorCode;
import com.ringdu.server.global.security.jwt.JwtProperties;
import com.ringdu.server.user.entity.Role;
import com.ringdu.server.user.entity.User;
import com.ringdu.server.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class RefreshTokenServiceTest {

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private InMemoryRefreshTokenStore refreshTokenStore;

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        refreshTokenStore.clear();
    }

    @Test
    @DisplayName("Refresh Token 생성 시 원문이 아닌 해시만 TTL과 함께 저장한다")
    void createRefreshTokenAndSaveHashWithTtl() {
        User user = saveUser("refresh@ringdu.com");

        var issue = refreshTokenService.createRefreshToken(user);
        String tokenHash = refreshTokenService.hashToken(issue.refreshToken());

        assertThat(issue.refreshToken()).isNotBlank();
        assertThat(refreshTokenStore.containsActiveToken(tokenHash)).isTrue();
        assertThat(refreshTokenStore.containsRawToken(issue.refreshToken())).isFalse();
        assertThat(refreshTokenStore.getTtl(tokenHash)).isPositive();
        assertThat(refreshTokenStore.getTtl(tokenHash)).isLessThanOrEqualTo(jwtProperties.refreshTokenTtl());
    }

    @Test
    @DisplayName("Refresh Token 회전 시 기존 active key는 삭제되고 used key와 새 active key가 TTL과 함께 저장된다")
    void rotateRefreshToken() {
        User user = saveUser("rotate@ringdu.com");
        var issue = refreshTokenService.createRefreshToken(user);
        String oldTokenHash = refreshTokenService.hashToken(issue.refreshToken());

        var rotation = refreshTokenService.rotateRefreshToken(issue.refreshToken());
        String newTokenHash = refreshTokenService.hashToken(rotation.refreshToken());

        assertThat(refreshTokenStore.containsActiveToken(oldTokenHash)).isFalse();
        assertThat(refreshTokenStore.containsUsedToken(oldTokenHash)).isTrue();
        assertThat(refreshTokenStore.getUsedTtl(oldTokenHash)).isPositive();
        assertThat(refreshTokenStore.containsActiveToken(newTokenHash)).isTrue();
        assertThat(refreshTokenStore.getTtl(newTokenHash)).isPositive();
        assertThat(rotation.refreshToken()).isNotEqualTo(issue.refreshToken());
        assertThat(rotation.userId()).isEqualTo(user.getId());
    }

    @Test
    @DisplayName("이미 사용된 Refresh Token으로 다시 회전하면 재사용 감지 예외가 발생한다")
    void throwExceptionWhenRefreshTokenReused() {
        User user = saveUser("reuse@ringdu.com");
        var issue = refreshTokenService.createRefreshToken(user);
        refreshTokenService.rotateRefreshToken(issue.refreshToken());

        assertThatThrownBy(() -> refreshTokenService.rotateRefreshToken(issue.refreshToken()))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.REFRESH_TOKEN_REUSE_DETECTED);
    }

    @Test
    @DisplayName("존재하지 않거나 만료된 Refresh Token은 예외가 발생한다")
    void throwExceptionWhenRefreshTokenMissingOrExpired() {
        User user = saveUser("expired@ringdu.com");
        var issue = refreshTokenService.createRefreshToken(user);
        refreshTokenStore.expireActiveToken(refreshTokenService.hashToken(issue.refreshToken()));

        assertThatThrownBy(() -> refreshTokenService.rotateRefreshToken(issue.refreshToken()))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.REFRESH_TOKEN_NOT_FOUND);

        assertThatThrownBy(() -> refreshTokenService.rotateRefreshToken("missing-refresh-token"))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.REFRESH_TOKEN_NOT_FOUND);
    }

    @Test
    @DisplayName("로그아웃 후 기존 Refresh Token으로 회전할 수 없다")
    void logoutDeletesRefreshToken() {
        User user = saveUser("logout@ringdu.com");
        var issue = refreshTokenService.createRefreshToken(user);
        String tokenHash = refreshTokenService.hashToken(issue.refreshToken());

        refreshTokenService.revokeRefreshToken(issue.refreshToken());

        assertThat(refreshTokenStore.containsActiveToken(tokenHash)).isFalse();
        assertThatThrownBy(() -> refreshTokenService.rotateRefreshToken(issue.refreshToken()))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.REFRESH_TOKEN_NOT_FOUND);
    }

    @Test
    @DisplayName("Access Token은 Refresh Token 저장소에 저장하지 않는다")
    void accessTokenIsNotStored() {
        User user = saveUser("access@ringdu.com");
        var issue = refreshTokenService.createRefreshToken(user);
        String accessTokenLikeValue = "access-token-value";

        assertThat(refreshTokenStore.containsRawToken(accessTokenLikeValue)).isFalse();
        assertThat(refreshTokenStore.containsRawToken(issue.refreshToken())).isFalse();
    }

    private User saveUser(String email) {
        return userRepository.save(User.createLocalUser(
                email,
                passwordEncoder.encode("password123"),
                "홍길동",
                "010-1234-5678",
                Role.STUDENT
        ));
    }
}

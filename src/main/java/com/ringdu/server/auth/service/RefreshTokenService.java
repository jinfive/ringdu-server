package com.ringdu.server.auth.service;

import com.ringdu.server.auth.dto.RefreshTokenIssue;
import com.ringdu.server.auth.dto.RefreshTokenRotation;
import com.ringdu.server.auth.repository.RefreshTokenStore;
import com.ringdu.server.global.exception.BusinessException;
import com.ringdu.server.global.exception.ErrorCode;
import com.ringdu.server.global.security.jwt.JwtProperties;
import com.ringdu.server.user.entity.User;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenStore refreshTokenStore;
    private final JwtProperties jwtProperties;

    public RefreshTokenIssue createRefreshToken(User user) {
        String refreshToken = generateRefreshToken();
        String tokenHash = hashToken(refreshToken);

        refreshTokenStore.save(tokenHash, user.getId(), jwtProperties.refreshTokenTtl());
        return new RefreshTokenIssue(refreshToken);
    }

    public RefreshTokenRotation rotateRefreshToken(String refreshToken) {
        validateRefreshTokenExists(refreshToken);

        String tokenHash = hashToken(refreshToken);
        Long userId = refreshTokenStore.findUserId(tokenHash)
                .orElseGet(() -> handleMissingActiveToken(tokenHash));

        Duration usedTokenTtl = resolveUsedRefreshTokenTtl(refreshTokenStore.getTtl(tokenHash));
        refreshTokenStore.delete(tokenHash);
        refreshTokenStore.markAsUsed(tokenHash, userId, usedTokenTtl);

        String newRefreshToken = generateRefreshToken();
        String newTokenHash = hashToken(newRefreshToken);
        refreshTokenStore.save(newTokenHash, userId, jwtProperties.refreshTokenTtl());

        return new RefreshTokenRotation(newRefreshToken, userId);
    }

    public void revokeRefreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return;
        }

        refreshTokenStore.delete(hashToken(refreshToken));
    }

    public void revokeAllActiveTokensByUser(User user) {
        refreshTokenStore.deleteActiveTokensByUserId(user.getId());
    }

    public String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 algorithm is not available", exception);
        }
    }

    private void validateRefreshTokenExists(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_NOT_FOUND);
        }
    }

    private Long handleMissingActiveToken(String tokenHash) {
        var usedUserId = refreshTokenStore.findUsedUserId(tokenHash);
        if (usedUserId.isPresent()) {
            refreshTokenStore.deleteActiveTokensByUserId(usedUserId.get());
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_REUSE_DETECTED);
        }

        throw new BusinessException(ErrorCode.REFRESH_TOKEN_NOT_FOUND);
    }

    private Duration resolveUsedRefreshTokenTtl(Duration remainingActiveTtl) {
        if (remainingActiveTtl != null && !remainingActiveTtl.isZero() && !remainingActiveTtl.isNegative()) {
            return remainingActiveTtl.compareTo(jwtProperties.usedRefreshTokenTtl()) < 0
                    ? remainingActiveTtl
                    : jwtProperties.usedRefreshTokenTtl();
        }

        if (jwtProperties.usedRefreshTokenTtl().isZero() || jwtProperties.usedRefreshTokenTtl().isNegative()) {
            throw new IllegalStateException("Used refresh token TTL must be positive");
        }

        return jwtProperties.usedRefreshTokenTtl();
    }

    private String generateRefreshToken() {
        return UUID.randomUUID() + "." + UUID.randomUUID();
    }
}

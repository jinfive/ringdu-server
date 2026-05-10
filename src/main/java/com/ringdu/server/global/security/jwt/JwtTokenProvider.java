package com.ringdu.server.global.security.jwt;

import com.ringdu.server.global.exception.BusinessException;
import com.ringdu.server.global.exception.ErrorCode;
import com.ringdu.server.user.entity.Role;
import com.ringdu.server.user.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

    private static final String USER_ID_CLAIM = "userId";
    private static final String ROLE_CLAIM = "role";

    private final SecretKey secretKey;
    private final JwtProperties jwtProperties;

    public JwtTokenProvider(JwtProperties jwtProperties) {
        this.secretKey = Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
        this.jwtProperties = jwtProperties;
    }

    public String createAccessToken(User user) {
        Date now = new Date();
        Date expiresAt = new Date(now.getTime() + jwtProperties.accessTokenTtl().toMillis());

        return Jwts.builder()
                .subject(user.getEmail())
                .claim(USER_ID_CLAIM, user.getId())
                .claim(ROLE_CLAIM, user.getRole().name())
                .issuedAt(now)
                .expiration(expiresAt)
                .signWith(secretKey)
                .compact();
    }

    public boolean validateToken(String token) {
        parseClaims(token);
        return true;
    }

    public Long getUserId(String token) {
        Object userId = parseClaims(token).get(USER_ID_CLAIM);
        if (userId instanceof Number number) {
            return number.longValue();
        }

        throw new BusinessException(ErrorCode.INVALID_TOKEN);
    }

    public String getEmail(String token) {
        return parseClaims(token).getSubject();
    }

    public Role getRole(String token) {
        return Role.valueOf(parseClaims(token).get(ROLE_CLAIM, String.class));
    }

    private Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException exception) {
            throw new BusinessException(ErrorCode.EXPIRED_TOKEN);
        } catch (JwtException | IllegalArgumentException exception) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }
    }
}

package com.ringdu.server.auth.repository;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@Profile("!test")
@RequiredArgsConstructor
public class RedisRefreshTokenStore implements RefreshTokenStore {

    private static final String REFRESH_KEY_PREFIX = "refresh:";
    private static final String USED_REFRESH_KEY_PREFIX = "used-refresh:";

    private final StringRedisTemplate redisTemplate;

    @Override
    public void save(String tokenHash, Long userId, Duration ttl) {
        validateTtl(ttl);
        redisTemplate.opsForValue().set(refreshKey(tokenHash), String.valueOf(userId), ttl);
    }

    @Override
    public Optional<Long> findUserId(String tokenHash) {
        return parseUserId(redisTemplate.opsForValue().get(refreshKey(tokenHash)));
    }

    @Override
    public Duration getTtl(String tokenHash) {
        return Optional.ofNullable(redisTemplate.getExpire(refreshKey(tokenHash)))
                .filter(seconds -> seconds > 0)
                .map(Duration::ofSeconds)
                .orElse(Duration.ZERO);
    }

    @Override
    public void delete(String tokenHash) {
        redisTemplate.delete(refreshKey(tokenHash));
    }

    @Override
    public void markAsUsed(String tokenHash, Long userId, Duration ttl) {
        validateTtl(ttl);
        redisTemplate.opsForValue().set(usedRefreshKey(tokenHash), String.valueOf(userId), ttl);
    }

    @Override
    public Optional<Long> findUsedUserId(String tokenHash) {
        return parseUserId(redisTemplate.opsForValue().get(usedRefreshKey(tokenHash)));
    }

    @Override
    public void deleteActiveTokensByUserId(Long userId) {
        redisTemplate.execute((RedisCallback<Void>) connection -> {
            ScanOptions options = ScanOptions.scanOptions().match(REFRESH_KEY_PREFIX + "*").count(100).build();
            try (Cursor<byte[]> cursor = connection.keyCommands().scan(options)) {
                while (cursor.hasNext()) {
                    byte[] key = cursor.next();
                    byte[] value = connection.stringCommands().get(key);
                    if (Objects.equals(String.valueOf(userId), value == null ? null : new String(value, StandardCharsets.UTF_8))) {
                        connection.keyCommands().del(key);
                    }
                }
            }
            return null;
        });
    }

    private Optional<Long> parseUserId(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(Long.valueOf(value));
    }

    private void validateTtl(Duration ttl) {
        if (ttl == null || ttl.isZero() || ttl.isNegative()) {
            throw new IllegalArgumentException("Refresh token TTL must be positive");
        }
    }

    private String refreshKey(String tokenHash) {
        return REFRESH_KEY_PREFIX + tokenHash;
    }

    private String usedRefreshKey(String tokenHash) {
        return USED_REFRESH_KEY_PREFIX + tokenHash;
    }
}

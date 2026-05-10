package com.ringdu.server.auth.repository;

import java.time.Duration;
import java.util.Optional;

public interface RefreshTokenStore {

    void save(String tokenHash, Long userId, Duration ttl);

    Optional<Long> findUserId(String tokenHash);

    Duration getTtl(String tokenHash);

    void delete(String tokenHash);

    void markAsUsed(String tokenHash, Long userId, Duration ttl);

    Optional<Long> findUsedUserId(String tokenHash);

    void deleteActiveTokensByUserId(Long userId);
}

package com.ringdu.server.auth.repository;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("test")
public class InMemoryRefreshTokenStore implements RefreshTokenStore {

    private final Map<String, StoredToken> activeTokens = new HashMap<>();
    private final Map<String, StoredToken> usedTokens = new HashMap<>();

    @Override
    public void save(String tokenHash, Long userId, Duration ttl) {
        activeTokens.put(tokenHash, StoredToken.of(userId, ttl));
    }

    @Override
    public Optional<Long> findUserId(String tokenHash) {
        return findValid(activeTokens, tokenHash);
    }

    @Override
    public Duration getTtl(String tokenHash) {
        StoredToken token = activeTokens.get(tokenHash);
        if (token == null || token.isExpired()) {
            activeTokens.remove(tokenHash);
            return Duration.ZERO;
        }
        return Duration.between(Instant.now(), token.expiresAt());
    }

    @Override
    public void delete(String tokenHash) {
        activeTokens.remove(tokenHash);
    }

    @Override
    public void markAsUsed(String tokenHash, Long userId, Duration ttl) {
        usedTokens.put(tokenHash, StoredToken.of(userId, ttl));
    }

    @Override
    public Optional<Long> findUsedUserId(String tokenHash) {
        return findValid(usedTokens, tokenHash);
    }

    @Override
    public void deleteActiveTokensByUserId(Long userId) {
        activeTokens.entrySet().removeIf(entry -> entry.getValue().userId().equals(userId));
    }

    public boolean containsActiveToken(String tokenHash) {
        return findUserId(tokenHash).isPresent();
    }

    public boolean containsUsedToken(String tokenHash) {
        return findUsedUserId(tokenHash).isPresent();
    }

    public Duration getUsedTtl(String tokenHash) {
        StoredToken token = usedTokens.get(tokenHash);
        if (token == null || token.isExpired()) {
            usedTokens.remove(tokenHash);
            return Duration.ZERO;
        }
        return Duration.between(Instant.now(), token.expiresAt());
    }

    public void expireActiveToken(String tokenHash) {
        StoredToken token = activeTokens.get(tokenHash);
        if (token != null) {
            activeTokens.put(tokenHash, new StoredToken(token.userId(), Instant.now().minusSeconds(1)));
        }
    }

    public void clear() {
        activeTokens.clear();
        usedTokens.clear();
    }

    public boolean containsRawToken(String rawToken) {
        return activeTokens.containsKey(rawToken) || usedTokens.containsKey(rawToken);
    }

    private Optional<Long> findValid(Map<String, StoredToken> tokens, String tokenHash) {
        purgeExpired(tokens);
        StoredToken token = tokens.get(tokenHash);
        return token == null ? Optional.empty() : Optional.of(token.userId());
    }

    private void purgeExpired(Map<String, StoredToken> tokens) {
        Iterator<Map.Entry<String, StoredToken>> iterator = tokens.entrySet().iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getValue().isExpired()) {
                iterator.remove();
            }
        }
    }

    private record StoredToken(Long userId, Instant expiresAt) {

        private static StoredToken of(Long userId, Duration ttl) {
            return new StoredToken(userId, Instant.now().plus(ttl));
        }

        private boolean isExpired() {
            return !expiresAt.isAfter(Instant.now());
        }
    }
}

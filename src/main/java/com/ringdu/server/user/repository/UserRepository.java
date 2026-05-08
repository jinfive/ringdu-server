package com.ringdu.server.user.repository;

import com.ringdu.server.user.entity.AuthProvider;
import com.ringdu.server.user.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    Optional<User> findByProviderAndProviderId(AuthProvider provider, String providerId);

    boolean existsByProviderAndProviderId(AuthProvider provider, String providerId);
}

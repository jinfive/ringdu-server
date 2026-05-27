package com.ringdu.server.user.repository;

import com.ringdu.server.user.entity.AuthProvider;
import com.ringdu.server.user.entity.Role;
import com.ringdu.server.user.entity.User;
import com.ringdu.server.user.entity.UserStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    long countByEmail(String email);

    Optional<User> findByEmail(String email);

    Optional<User> findByEmailAndRoleAndStatus(String email, Role role, UserStatus status);

    Optional<User> findByPhoneAndRoleAndStatus(String phone, Role role, UserStatus status);

    List<User> findAllByPhoneAndRoleAndStatus(String phone, Role role, UserStatus status);

    @Query("""
            select distinct u
            from User u
            where u.role = :role
              and u.status = :status
              and (
                    (:email is not null and u.email = :email)
                    or (:phone is not null and u.phone = :phone)
              )
            """)
    List<User> findDistinctCandidatesByRoleAndStatusAndEmailOrPhone(
            @Param("role") Role role,
            @Param("status") UserStatus status,
            @Param("email") String email,
            @Param("phone") String phone
    );

    Optional<User> findByProviderAndProviderId(AuthProvider provider, String providerId);

    boolean existsByProviderAndProviderId(AuthProvider provider, String providerId);
}

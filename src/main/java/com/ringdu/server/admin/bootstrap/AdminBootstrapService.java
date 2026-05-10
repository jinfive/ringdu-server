package com.ringdu.server.admin.bootstrap;

import com.ringdu.server.user.entity.Role;
import com.ringdu.server.user.entity.User;
import com.ringdu.server.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminBootstrapService {

    public static final String DEFAULT_ADMIN_EMAIL = "admin@ringdu.com";
    public static final String DEFAULT_ADMIN_PASSWORD = "ringdu1234";
    public static final String DEFAULT_ADMIN_NAME = "Ringdu Admin";
    private static final String DEFAULT_ADMIN_PHONE = "010-0000-0000";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void initializeDefaultAdmin() {
        if (userRepository.existsByEmail(DEFAULT_ADMIN_EMAIL)) {
            return;
        }

        User admin = User.createLocalUser(
                DEFAULT_ADMIN_EMAIL,
                passwordEncoder.encode(DEFAULT_ADMIN_PASSWORD),
                DEFAULT_ADMIN_NAME,
                DEFAULT_ADMIN_PHONE,
                Role.ADMIN
        );
        userRepository.save(admin);
    }
}

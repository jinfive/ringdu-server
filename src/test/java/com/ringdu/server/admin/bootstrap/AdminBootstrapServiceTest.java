package com.ringdu.server.admin.bootstrap;

import static org.assertj.core.api.Assertions.assertThat;

import com.ringdu.server.user.entity.Role;
import com.ringdu.server.user.entity.User;
import com.ringdu.server.user.repository.UserRepository;
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
class AdminBootstrapServiceTest {

    @Autowired
    private AdminBootstrapService adminBootstrapService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("ADMIN 기본 계정이 없으면 생성한다")
    void initializeDefaultAdminWhenNotExists() {
        userRepository.findByEmail(AdminBootstrapService.DEFAULT_ADMIN_EMAIL)
                .ifPresent(userRepository::delete);
        userRepository.flush();

        adminBootstrapService.initializeDefaultAdmin();

        User admin = userRepository.findByEmail(AdminBootstrapService.DEFAULT_ADMIN_EMAIL).orElseThrow();
        assertThat(admin.getName()).isEqualTo(AdminBootstrapService.DEFAULT_ADMIN_NAME);
        assertThat(admin.getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    @DisplayName("ADMIN 기본 계정이 이미 있으면 중복 생성하지 않는다")
    void doNotCreateDuplicatedDefaultAdmin() {
        adminBootstrapService.initializeDefaultAdmin();
        adminBootstrapService.initializeDefaultAdmin();

        assertThat(userRepository.countByEmail(AdminBootstrapService.DEFAULT_ADMIN_EMAIL)).isEqualTo(1);
    }

    @Test
    @DisplayName("ADMIN 기본 계정 비밀번호는 BCrypt 해시로 저장된다")
    void encodeDefaultAdminPassword() {
        adminBootstrapService.initializeDefaultAdmin();

        User admin = userRepository.findByEmail(AdminBootstrapService.DEFAULT_ADMIN_EMAIL).orElseThrow();
        assertThat(admin.getPassword()).isNotEqualTo(AdminBootstrapService.DEFAULT_ADMIN_PASSWORD);
        assertThat(passwordEncoder.matches(AdminBootstrapService.DEFAULT_ADMIN_PASSWORD, admin.getPassword()))
                .isTrue();
    }
}

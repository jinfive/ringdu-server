package com.ringdu.server.global.security.jwt;

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
class JwtTokenProviderTest {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("Access Token을 생성하고 claim을 추출할 수 있다")
    void createAccessTokenAndExtractClaims() {
        User user = userRepository.save(User.createLocalUser(
                "jwt@ringdu.com",
                passwordEncoder.encode("password123"),
                "홍길동",
                "010-1234-5678",
                Role.TEACHER
        ));

        String token = jwtTokenProvider.createAccessToken(user);

        assertThat(token).isNotBlank();
        assertThat(jwtTokenProvider.getUserId(token)).isEqualTo(user.getId());
        assertThat(jwtTokenProvider.getEmail(token)).isEqualTo("jwt@ringdu.com");
        assertThat(jwtTokenProvider.getRole(token)).isEqualTo(Role.TEACHER);
    }
}

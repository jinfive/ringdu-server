package com.ringdu.server.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ringdu.server.auth.dto.SignupRequest;
import com.ringdu.server.auth.dto.SignupResponse;
import com.ringdu.server.global.exception.BusinessException;
import com.ringdu.server.global.exception.ErrorCode;
import com.ringdu.server.user.entity.AuthProvider;
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
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("TEACHER 권한으로 회원가입할 수 있다")
    void signupTeacher() {
        SignupResponse response = authService.signup(signupRequest("teacher@ringdu.com", Role.TEACHER));

        assertThat(response.email()).isEqualTo("teacher@ringdu.com");
        assertThat(response.role()).isEqualTo(Role.TEACHER);
    }

    @Test
    @DisplayName("PARENT 권한으로 회원가입할 수 있다")
    void signupParent() {
        SignupResponse response = authService.signup(signupRequest("parent@ringdu.com", Role.PARENT));

        assertThat(response.email()).isEqualTo("parent@ringdu.com");
        assertThat(response.role()).isEqualTo(Role.PARENT);
    }

    @Test
    @DisplayName("STUDENT 권한으로 회원가입할 수 있다")
    void signupStudent() {
        SignupResponse response = authService.signup(signupRequest("student@ringdu.com", Role.STUDENT));

        assertThat(response.email()).isEqualTo("student@ringdu.com");
        assertThat(response.role()).isEqualTo(Role.STUDENT);
    }

    @Test
    @DisplayName("이메일이 중복되면 예외가 발생한다")
    void throwExceptionWhenEmailDuplicated() {
        authService.signup(signupRequest("duplicated@ringdu.com", Role.TEACHER));

        assertThatThrownBy(() -> authService.signup(signupRequest("duplicated@ringdu.com", Role.PARENT)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.DUPLICATED_EMAIL);
    }

    @Test
    @DisplayName("ADMIN 권한은 일반 회원가입으로 생성할 수 없다")
    void throwExceptionWhenAdminSignup() {
        assertSignupRoleNotAllowed(Role.ADMIN);
    }

    @Test
    @DisplayName("OWNER 권한은 일반 회원가입으로 생성할 수 없다")
    void throwExceptionWhenOwnerSignup() {
        assertSignupRoleNotAllowed(Role.OWNER);
    }

    @Test
    @DisplayName("DESK 권한은 일반 회원가입으로 생성할 수 없다")
    void throwExceptionWhenDeskSignup() {
        assertSignupRoleNotAllowed(Role.DESK);
    }

    @Test
    @DisplayName("비밀번호는 BCrypt로 암호화되어 저장된다")
    void encodePasswordWithBCrypt() {
        authService.signup(signupRequest("encoded@ringdu.com", Role.TEACHER));

        User user = userRepository.findByEmail("encoded@ringdu.com").orElseThrow();

        assertThat(user.getPassword()).isNotEqualTo("password123");
        assertThat(passwordEncoder.matches("password123", user.getPassword())).isTrue();
    }

    @Test
    @DisplayName("일반 회원가입 사용자의 provider는 LOCAL로 저장된다")
    void saveLocalProviderWhenSignup() {
        authService.signup(signupRequest("local@ringdu.com", Role.PARENT));

        User user = userRepository.findByEmail("local@ringdu.com").orElseThrow();

        assertThat(user.getProvider()).isEqualTo(AuthProvider.LOCAL);
    }

    @Test
    @DisplayName("일반 회원가입 사용자의 providerId는 null로 저장된다")
    void saveNullProviderIdWhenSignup() {
        authService.signup(signupRequest("provider-id@ringdu.com", Role.STUDENT));

        User user = userRepository.findByEmail("provider-id@ringdu.com").orElseThrow();

        assertThat(user.getProviderId()).isNull();
    }

    private void assertSignupRoleNotAllowed(Role role) {
        assertThatThrownBy(() -> authService.signup(signupRequest(role.name().toLowerCase() + "@ringdu.com", role)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.SIGNUP_ROLE_NOT_ALLOWED);
    }

    private SignupRequest signupRequest(String email, Role role) {
        return new SignupRequest(
                email,
                "password123",
                "홍길동",
                "010-1234-5678",
                role
        );
    }
}

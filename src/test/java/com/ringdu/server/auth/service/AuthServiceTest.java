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
import com.ringdu.server.user.entity.UserStatus;
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
    @DisplayName("같은 이메일과 같은 비밀번호로 다시 가입해도 실패한다")
    void throwExceptionWhenSameEmailAndSamePasswordSignupAgain() {
        authService.signup(signupRequest("same-password@ringdu.com", "password123", Role.TEACHER));

        assertThatThrownBy(() -> authService.signup(signupRequest(
                "same-password@ringdu.com",
                "password123",
                Role.STUDENT
        )))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.DUPLICATED_EMAIL);
    }

    @Test
    @DisplayName("같은 이메일과 다른 비밀번호로 다시 가입해도 실패한다")
    void throwExceptionWhenSameEmailAndDifferentPasswordSignupAgain() {
        authService.signup(signupRequest("different-password@ringdu.com", "password123", Role.PARENT));

        assertThatThrownBy(() -> authService.signup(signupRequest(
                "different-password@ringdu.com",
                "different123",
                Role.STUDENT
        )))
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
    @DisplayName("비밀번호 원문은 DB에 저장되지 않는다")
    void doNotSaveRawPassword() {
        authService.signup(signupRequest("raw-password@ringdu.com", "password123", Role.STUDENT));

        User user = userRepository.findByEmail("raw-password@ringdu.com").orElseThrow();

        assertThat(user.getPassword()).isNotEqualTo("password123");
        assertThat(user.getPassword()).doesNotContain("password123");
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

    @Test
    @DisplayName("로그인 성공 시 accessToken과 refreshToken이 반환된다")
    void loginSuccess() {
        authService.signup(signupRequest("login@ringdu.com", Role.STUDENT));

        var result = authService.login(new com.ringdu.server.auth.dto.LoginRequest("login@ringdu.com", "password123"));

        assertThat(result.loginResponse().accessToken()).isNotBlank();
        assertThat(result.refreshToken()).isNotBlank();
        assertThat(result.loginResponse().tokenType()).isEqualTo("Bearer");
    }

    @Test
    @DisplayName("이메일이 없으면 로그인 예외가 발생한다")
    void throwExceptionWhenLoginEmailNotFound() {
        assertThatThrownBy(() -> authService.login(new com.ringdu.server.auth.dto.LoginRequest("none@ringdu.com", "password123")))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("비밀번호가 틀리면 로그인 예외가 발생한다")
    void throwExceptionWhenPasswordInvalid() {
        authService.signup(signupRequest("invalid-password@ringdu.com", Role.TEACHER));

        assertThatThrownBy(() -> authService.login(new com.ringdu.server.auth.dto.LoginRequest("invalid-password@ringdu.com", "wrong-password")))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_PASSWORD);
    }

    @Test
    @DisplayName("비활성화 계정은 로그인할 수 없다")
    void throwExceptionWhenInactiveUserLogin() {
        User user = User.createLocalUser(
                "inactive@ringdu.com",
                passwordEncoder.encode("password123"),
                "홍길동",
                "010-1234-5678",
                Role.PARENT
        );
        user.deactivate();
        userRepository.save(user);

        assertThat(user.getStatus()).isEqualTo(UserStatus.INACTIVE);
        assertThatThrownBy(() -> authService.login(new com.ringdu.server.auth.dto.LoginRequest("inactive@ringdu.com", "password123")))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INACTIVE_USER);
    }

    @Test
    @DisplayName("소셜 provider 계정은 일반 로그인할 수 없다")
    void throwExceptionWhenSocialUserLocalLogin() {
        User user = User.createSocialUser(
                "social@ringdu.com",
                "홍길동",
                "010-1234-5678",
                Role.STUDENT,
                AuthProvider.KAKAO,
                "kakao-1"
        );
        userRepository.save(user);

        assertThatThrownBy(() -> authService.login(new com.ringdu.server.auth.dto.LoginRequest("social@ringdu.com", "password123")))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.LOCAL_LOGIN_NOT_ALLOWED);
    }

    private void assertSignupRoleNotAllowed(Role role) {
        assertThatThrownBy(() -> authService.signup(signupRequest(role.name().toLowerCase() + "@ringdu.com", role)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.SIGNUP_ROLE_NOT_ALLOWED);
    }

    private SignupRequest signupRequest(String email, Role role) {
        return signupRequest(email, "password123", role);
    }

    private SignupRequest signupRequest(String email, String password, Role role) {
        return new SignupRequest(
                email,
                password,
                "홍길동",
                "010-1234-5678",
                role
        );
    }
}

package com.ringdu.server.admin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ringdu.server.academy.entity.AcademySignupApplication;
import com.ringdu.server.academy.entity.AcademySignupApplicationStatus;
import com.ringdu.server.academy.repository.AcademyRepository;
import com.ringdu.server.academy.repository.AcademySignupApplicationRepository;
import com.ringdu.server.auth.dto.AcademySignupRequest;
import com.ringdu.server.auth.dto.LoginRequest;
import com.ringdu.server.auth.service.AuthService;
import com.ringdu.server.global.exception.BusinessException;
import com.ringdu.server.global.exception.ErrorCode;
import com.ringdu.server.user.entity.User;
import com.ringdu.server.user.entity.UserStatus;
import com.ringdu.server.user.repository.UserRepository;
import java.util.Arrays;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class AdminAcademySignupApplicationServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private AdminAcademySignupApplicationService adminAcademySignupApplicationService;

    @Autowired
    private AcademySignupApplicationRepository academySignupApplicationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AcademyRepository academyRepository;

    @Test
    @DisplayName("ADMIN은 승인 대기 학원 가입 신청 목록을 조회할 수 있다")
    void findPendingApplications() {
        authService.signupAcademy(academySignupRequest("pending-list@ringdu.com"));

        var responses = adminAcademySignupApplicationService.findPendingApplications();

        assertThat(responses).anySatisfy(response -> {
            assertThat(response.email()).isEqualTo("pending-list@ringdu.com");
            assertThat(response.status()).isEqualTo(AcademySignupApplicationStatus.PENDING);
        });
    }

    @Test
    @DisplayName("ADMIN은 학원 가입 신청을 승인할 수 있다")
    void approveApplication() {
        var signupResponse = authService.signupAcademy(academySignupRequest("approve-academy@ringdu.com"));

        var response = adminAcademySignupApplicationService.approve(signupResponse.applicationId(), 1L);

        assertThat(response.applicationStatus()).isEqualTo(AcademySignupApplicationStatus.APPROVED);
        assertThat(response.userStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(response.approvedAt()).isNotNull();

        User user = userRepository.findByEmail("approve-academy@ringdu.com").orElseThrow();
        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(academyRepository.findByUserId(user.getId())).hasValueSatisfying(academy -> {
            assertThat(academy.getUser().getId()).isEqualTo(user.getId());
            assertThat(academy.getName()).isEqualTo("링듀수학학원");
            assertThat(academy.getRepresentativeName()).isEqualTo("홍길동");
            assertThat(academy.getPhone()).isEqualTo("010-1234-5678");
            assertThat(academy.getPostalCode()).isEqualTo("06123");
            assertThat(academy.getAddress()).isEqualTo("서울시 강남구 테헤란로");
            assertThat(academy.getDetailAddress()).isEqualTo("101호");
        });
    }

    @Test
    @DisplayName("승인 후 ACADEMY 계정은 로그인할 수 있다")
    void approvedAcademyCanLogin() {
        var signupResponse = authService.signupAcademy(academySignupRequest("approved-login@ringdu.com"));
        adminAcademySignupApplicationService.approve(signupResponse.applicationId(), 1L);

        var loginResult = authService.login(new LoginRequest("approved-login@ringdu.com", "password1234"));

        assertThat(loginResult.loginResponse().role().name()).isEqualTo("ACADEMY");
        assertThat(loginResult.loginResponse().accessToken()).isNotBlank();
    }

    @Test
    @DisplayName("이미 승인된 학원 가입 신청은 다시 승인할 수 없다")
    void cannotApproveAlreadyApprovedApplication() {
        var signupResponse = authService.signupAcademy(academySignupRequest("already-approved@ringdu.com"));
        adminAcademySignupApplicationService.approve(signupResponse.applicationId(), 1L);
        User user = userRepository.findByEmail("already-approved@ringdu.com").orElseThrow();
        long academyCount = academyRepository.count();

        assertThatThrownBy(() -> adminAcademySignupApplicationService.approve(signupResponse.applicationId(), 1L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ACADEMY_SIGNUP_APPLICATION_ALREADY_REVIEWED);
        assertThat(academyRepository.count()).isEqualTo(academyCount);
        assertThat(academyRepository.findByUserId(user.getId())).isPresent();
    }

    @Test
    @DisplayName("학원 가입 신청 엔티티에는 주민등록번호 필드가 없다")
    void academySignupApplicationDoesNotHaveResidentRegistrationNumberField() {
        assertThat(Arrays.stream(AcademySignupApplication.class.getDeclaredFields())
                .map(field -> field.getName().toLowerCase())
                .anyMatch(fieldName -> fieldName.contains("resident") || fieldName.contains("rrn")))
                .isFalse();
    }

    private AcademySignupRequest academySignupRequest(String email) {
        return new AcademySignupRequest(
                email,
                "password1234",
                "password1234",
                "링듀수학학원",
                "홍길동",
                "010-1234-5678",
                "06123",
                "서울시 강남구 테헤란로",
                "101호"
        );
    }
}

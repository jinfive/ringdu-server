package com.ringdu.server.academy.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.ringdu.server.academy.dto.AcademyUpdateRequest;
import com.ringdu.server.academy.repository.AcademyRepository;
import com.ringdu.server.admin.service.AdminAcademySignupApplicationService;
import com.ringdu.server.auth.dto.AcademySignupRequest;
import com.ringdu.server.auth.service.AuthService;
import com.ringdu.server.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class AcademyServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private AdminAcademySignupApplicationService adminAcademySignupApplicationService;

    @Autowired
    private AcademyService academyService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AcademyRepository academyRepository;

    @Test
    @DisplayName("ACADEMY는 내 학원 정보를 조회할 수 있다")
    void getMyAcademy() {
        Long userId = approvedAcademyUserId("my-academy-service@ringdu.com");

        var response = academyService.getMyAcademy(userId);

        assertThat(response.name()).isEqualTo("링듀수학학원");
        assertThat(response.representativeName()).isEqualTo("홍길동");
        assertThat(response.academyId()).isNotNull();
    }

    @Test
    @DisplayName("ACADEMY는 내 학원 정보를 수정할 수 있다")
    void updateMyAcademy() {
        Long userId = approvedAcademyUserId("update-academy-service@ringdu.com");

        academyService.updateMyAcademy(userId, new AcademyUpdateRequest(
                "수정 학원",
                "김대표",
                "010-9999-8888",
                "12345",
                "서울시 서초구",
                "3층"
        ));

        var response = academyService.getMyAcademy(userId);
        assertThat(response.name()).isEqualTo("수정 학원");
        assertThat(response.representativeName()).isEqualTo("김대표");
        assertThat(response.phone()).isEqualTo("010-9999-8888");
        assertThat(response.postalCode()).isEqualTo("12345");
        assertThat(response.address()).isEqualTo("서울시 서초구");
        assertThat(response.detailAddress()).isEqualTo("3층");
    }

    @Test
    @DisplayName("대시보드 요약은 초기 0건 기반 응답을 반환한다")
    void getMyDashboard() {
        Long userId = approvedAcademyUserId("dashboard-academy-service@ringdu.com");

        var response = academyService.getMyDashboard(userId);

        assertThat(response.studentCount()).isZero();
        assertThat(response.teacherCount()).isZero();
        assertThat(response.unpaidInvoiceCount()).isZero();
        assertThat(response.pendingConsultationCount()).isZero();
        assertThat(response.notifications()).isNotEmpty();
    }

    private Long approvedAcademyUserId(String email) {
        var signupResponse = authService.signupAcademy(academySignupRequest(email));
        adminAcademySignupApplicationService.approve(signupResponse.applicationId(), 1L);
        Long userId = userRepository.findByEmail(email).orElseThrow().getId();
        assertThat(academyRepository.findByUserId(userId)).isPresent();
        return userId;
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

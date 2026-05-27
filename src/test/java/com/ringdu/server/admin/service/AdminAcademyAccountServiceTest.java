package com.ringdu.server.admin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ringdu.server.academy.repository.AcademyRepository;
import com.ringdu.server.admin.dto.CreateAcademyAccountRequest;
import com.ringdu.server.global.exception.BusinessException;
import com.ringdu.server.global.exception.ErrorCode;
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
class AdminAcademyAccountServiceTest {

    @Autowired
    private AdminAcademyAccountService adminAcademyAccountService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AcademyRepository academyRepository;

    @Test
    @DisplayName("ADMIN은 ACADEMY 계정을 생성할 수 있다")
    void createAcademyAccount() {
        var response = adminAcademyAccountService.createAcademyAccount(request("academy-created@ringdu.com"));

        assertThat(response.email()).isEqualTo("academy-created@ringdu.com");
        assertThat(response.role()).isEqualTo(Role.ACADEMY);
        assertThat(response.status()).isNotNull();

        User user = userRepository.findByEmail("academy-created@ringdu.com").orElseThrow();
        assertThat(user.getRole()).isEqualTo(Role.ACADEMY);
        assertThat(user.getPassword()).isNotEqualTo("password1234");
        assertThat(passwordEncoder.matches("password1234", user.getPassword())).isTrue();
        assertThat(academyRepository.findByUserId(user.getId())).hasValueSatisfying(academy -> {
            assertThat(academy.getName()).isEqualTo("링듀수학학원");
            assertThat(academy.getRepresentativeName()).isEqualTo("링듀수학학원");
            assertThat(academy.getPhone()).isEqualTo("010-1234-5678");
        });
    }

    @Test
    @DisplayName("중복 이메일로 ACADEMY 계정을 생성할 수 없다")
    void throwExceptionWhenDuplicatedEmail() {
        adminAcademyAccountService.createAcademyAccount(request("duplicated-academy@ringdu.com"));

        assertThatThrownBy(() -> adminAcademyAccountService.createAcademyAccount(request("duplicated-academy@ringdu.com")))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.DUPLICATED_EMAIL);
    }

    @Test
    @DisplayName("중복 전화번호로 ACADEMY 계정을 생성할 수 없다")
    void throwExceptionWhenDuplicatedPhone() {
        adminAcademyAccountService.createAcademyAccount(request("phone-source-academy@ringdu.com", "010-7777-0000"));

        assertThatThrownBy(() -> adminAcademyAccountService.createAcademyAccount(request(
                "phone-duplicated-academy@ringdu.com",
                "010-7777-0000"
        )))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.DUPLICATED_PHONE);
    }

    private CreateAcademyAccountRequest request(String email) {
        return request(email, "010-1234-5678");
    }

    private CreateAcademyAccountRequest request(String email, String phone) {
        return new CreateAcademyAccountRequest(
                email,
                "password1234",
                "링듀수학학원",
                phone
        );
    }
}

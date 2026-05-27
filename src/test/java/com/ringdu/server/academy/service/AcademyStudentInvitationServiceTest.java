package com.ringdu.server.academy.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ringdu.server.academy.dto.AcademyStudentInvitationCreateRequest;
import com.ringdu.server.academy.entity.Academy;
import com.ringdu.server.academy.entity.AcademyStudentInvitationStatus;
import com.ringdu.server.academy.repository.AcademyRepository;
import com.ringdu.server.academy.repository.AcademyStudentInvitationRepository;
import com.ringdu.server.admin.service.AdminAcademySignupApplicationService;
import com.ringdu.server.auth.dto.AcademySignupRequest;
import com.ringdu.server.auth.service.AuthService;
import com.ringdu.server.global.exception.BusinessException;
import com.ringdu.server.global.exception.ErrorCode;
import com.ringdu.server.student.entity.StudentProfile;
import com.ringdu.server.student.repository.StudentProfileRepository;
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
class AcademyStudentInvitationServiceTest {

    @Autowired
    private AcademyStudentInvitationService invitationService;

    @Autowired
    private AuthService authService;

    @Autowired
    private AdminAcademySignupApplicationService adminAcademySignupApplicationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AcademyRepository academyRepository;

    @Autowired
    private AcademyStudentInvitationRepository invitationRepository;

    @Autowired
    private StudentProfileRepository studentProfileRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("ACADEMY는 학생 초대장을 생성할 수 있다")
    void academyCanCreateInvitation() {
        Long academyUserId = approvedAcademyUserId("student-invitation-academy@ringdu.com");
        User studentUser = saveUser("invited-student@ringdu.com", Role.STUDENT);

        var response = invitationService.createInvitation(
                academyUserId,
                new AcademyStudentInvitationCreateRequest(
                        null,
                        studentUser.getId(),
                        studentUser.getEmail(),
                        studentUser.getPhone(),
                        "우리 학원에 오세요"
                )
        );

        assertThat(response.id()).isNotNull();
        assertThat(response.receiverUserId()).isEqualTo(studentUser.getId());
        assertThat(response.status()).isEqualTo(AcademyStudentInvitationStatus.PENDING);
    }

    @Test
    @DisplayName("이미 대기 중인 초대장이 있으면 중복 생성할 수 없다")
    void cannotCreateDuplicatedPendingInvitation() {
        Long academyUserId = approvedAcademyUserId("student-dup-academy@ringdu.com");
        User studentUser = saveUser("dup-student@ringdu.com", Role.STUDENT);

        invitationService.createInvitation(
                academyUserId,
                new AcademyStudentInvitationCreateRequest(null, studentUser.getId(), studentUser.getEmail(), studentUser.getPhone(), "초대1")
        );

        assertThatThrownBy(() -> invitationService.createInvitation(
                academyUserId,
                new AcademyStudentInvitationCreateRequest(null, studentUser.getId(), studentUser.getEmail(), studentUser.getPhone(), "초대2")
        ))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("이미 연결된 학생은 초대할 수 없다")
    void cannotInviteAlreadyConnectedStudent() {
        Long academyUserId = approvedAcademyUserId("student-connected-academy@ringdu.com");
        Academy academy = academyRepository.findByUserId(academyUserId).orElseThrow();
        User studentUser = saveUser("connected-student@ringdu.com", Role.STUDENT);
        
        studentProfileRepository.save(StudentProfile.builder()
                .academyId(academy.getId())
                .userId(studentUser.getId())
                .name(studentUser.getName())
                .status(com.ringdu.server.student.entity.StudentStatus.ACTIVE)
                .build());

        assertThatThrownBy(() -> invitationService.createInvitation(
                academyUserId,
                new AcademyStudentInvitationCreateRequest(null, studentUser.getId(), studentUser.getEmail(), studentUser.getPhone(), "초대")
        ))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("STUDENT는 초대장을 수락할 수 있고 StudentProfile이 연결된다")
    void studentCanAcceptInvitation() {
        Long academyUserId = approvedAcademyUserId("accept-academy@ringdu.com");
        Academy academy = academyRepository.findByUserId(academyUserId).orElseThrow();
        User studentUser = saveUser("accept-student@ringdu.com", Role.STUDENT);
        
        // Non-member profile
        StudentProfile profile = studentProfileRepository.save(StudentProfile.builder()
                .academyId(academy.getId())
                .name("학생이름")
                .status(com.ringdu.server.student.entity.StudentStatus.ACTIVE)
                .build());

        var invitationResponse = invitationService.createInvitation(
                academyUserId,
                new AcademyStudentInvitationCreateRequest(
                        profile.getId(),
                        studentUser.getId(),
                        studentUser.getEmail(),
                        studentUser.getPhone(),
                        "초대"
                )
        );

        invitationService.acceptInvitation(studentUser.getId(), invitationResponse.id());

        assertThat(invitationRepository.findById(invitationResponse.id()).orElseThrow().getStatus())
                .isEqualTo(AcademyStudentInvitationStatus.ACCEPTED);
        
        StudentProfile updatedProfile = studentProfileRepository.findById(profile.getId()).orElseThrow();
        assertThat(updatedProfile.getUserId()).isEqualTo(studentUser.getId());
    }

    @Test
    @DisplayName("STUDENT가 초대 수락 시 프로필이 없으면 새로 생성된다")
    void studentAcceptInvitationCreatesProfile() {
        Long academyUserId = approvedAcademyUserId("accept-new-academy@ringdu.com");
        Academy academy = academyRepository.findByUserId(academyUserId).orElseThrow();
        User studentUser = saveUser("accept-new-student@ringdu.com", Role.STUDENT);

        var invitationResponse = invitationService.createInvitation(
                academyUserId,
                new AcademyStudentInvitationCreateRequest(
                        null,
                        studentUser.getId(),
                        studentUser.getEmail(),
                        studentUser.getPhone(),
                        "초대"
                )
        );

        invitationService.acceptInvitation(studentUser.getId(), invitationResponse.id());

        assertThat(studentProfileRepository.existsByAcademyIdAndUserId(academy.getId(), studentUser.getId())).isTrue();
    }

    private Long approvedAcademyUserId(String email) {
        var signupResponse = authService.signupAcademy(new AcademySignupRequest(
                email, "password1234", "password1234", "학원명", "원장", "010-1111-2222", "12345", "주소", "상세"
        ));
        adminAcademySignupApplicationService.approve(signupResponse.applicationId(), 1L);
        return userRepository.findByEmail(email).orElseThrow().getId();
    }

    private User saveUser(String email, Role role) {
        return userRepository.save(User.createLocalUser(
                email,
                passwordEncoder.encode("password1234"),
                "테스트 사용자",
                "010-1234-5678",
                role
        ));
    }
}

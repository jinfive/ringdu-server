package com.ringdu.server.academy.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ringdu.server.academy.dto.TeacherInvitationCreateRequest;
import com.ringdu.server.academy.entity.AcademyTeacherInvitationStatus;
import com.ringdu.server.academy.repository.AcademyMemberRepository;
import com.ringdu.server.academy.repository.AcademyRepository;
import com.ringdu.server.academy.repository.AcademyTeacherInvitationRepository;
import com.ringdu.server.admin.service.AdminAcademySignupApplicationService;
import com.ringdu.server.auth.dto.AcademySignupRequest;
import com.ringdu.server.auth.service.AuthService;
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
class AcademyTeacherInvitationServiceTest {

    @Autowired
    private AcademyTeacherInvitationService invitationService;

    @Autowired
    private AuthService authService;

    @Autowired
    private AdminAcademySignupApplicationService adminAcademySignupApplicationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AcademyRepository academyRepository;

    @Autowired
    private AcademyTeacherInvitationRepository invitationRepository;

    @Autowired
    private AcademyMemberRepository academyMemberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("ACADEMY는 teacherPhone만으로 비회원 선생님 초대장을 생성할 수 있다")
    void academyCanCreateNonMemberInvitationByPhone() {
        Long academyUserId = approvedAcademyUserId("invitation-create-academy@ringdu.com");

        var response = invitationService.createInvitation(
                academyUserId,
                invitationRequest(null, "010-2000-0001")
        );

        assertThat(response.invitationId()).isNotNull();
        assertThat(response.teacherUserId()).isNull();
        assertThat(response.teacherEmail()).isNull();
        assertThat(response.teacherPhone()).isEqualTo("010-2000-0001");
        assertThat(response.status()).isEqualTo(AcademyTeacherInvitationStatus.PENDING);
        assertThat(response.expiresAt()).isNotNull();
    }

    @Test
    @DisplayName("teacherUserId가 있으면 ACTIVE TEACHER와 phone 일치를 검증하고 초대장을 생성한다")
    void academyCanCreateInvitationForSelectedTeacherCandidate() {
        Long academyUserId = approvedAcademyUserId("invitation-selected-academy@ringdu.com");
        User teacher = saveUser("selected-teacher@ringdu.com", "010-2000-0002", Role.TEACHER);

        var response = invitationService.createInvitation(
                academyUserId,
                invitationRequest(teacher.getId(), teacher.getPhone())
        );

        assertThat(response.teacherUserId()).isEqualTo(teacher.getId());
        assertThat(response.teacherEmail()).isEqualTo(teacher.getEmail());
        assertThat(response.teacherPhone()).isEqualTo(teacher.getPhone());
    }

    @Test
    @DisplayName("teacherUserId와 teacherPhone이 일치하지 않으면 초대장을 생성할 수 없다")
    void cannotCreateInvitationWhenSelectedTeacherPhoneMismatched() {
        Long academyUserId = approvedAcademyUserId("invitation-phone-mismatch-academy@ringdu.com");
        User teacher = saveUser("phone-mismatch-teacher@ringdu.com", "010-2000-0003", Role.TEACHER);

        assertThatThrownBy(() -> invitationService.createInvitation(
                academyUserId,
                invitationRequest(teacher.getId(), "010-2000-9999")
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_INPUT_VALUE);
    }

    @Test
    @DisplayName("같은 학원과 같은 teacherPhone의 PENDING 초대장은 중복 생성할 수 없다")
    void cannotCreateDuplicatedPendingInvitationByPhone() {
        Long academyUserId = approvedAcademyUserId("invitation-duplicate-academy@ringdu.com");
        invitationService.createInvitation(academyUserId, invitationRequest(null, "010-2000-0004"));

        assertThatThrownBy(() -> invitationService.createInvitation(
                academyUserId,
                invitationRequest(null, "010-2000-0004")
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.TEACHER_INVITATION_ALREADY_EXISTS);
    }

    @Test
    @DisplayName("이미 같은 학원에 연결된 선생님은 다시 초대할 수 없다")
    void cannotInviteAlreadyConnectedTeacher() {
        Long academyUserId = approvedAcademyUserId("invitation-connected-academy@ringdu.com");
        User teacher = saveUser("connected-teacher@ringdu.com", "010-2000-0005", Role.TEACHER);
        Long invitationId = invitationService.createInvitation(
                academyUserId,
                invitationRequest(teacher.getId(), teacher.getPhone())
        ).invitationId();
        invitationService.acceptInvitation(teacher.getId(), invitationId);

        assertThatThrownBy(() -> invitationService.createInvitation(
                academyUserId,
                invitationRequest(teacher.getId(), teacher.getPhone())
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.TEACHER_ALREADY_CONNECTED);
    }

    @Test
    @DisplayName("TEACHER는 자기 phone으로 온 초대장 목록을 조회할 수 있다")
    void teacherCanGetOwnInvitationsByPhone() {
        Long academyUserId = approvedAcademyUserId("teacher-invitation-list-academy@ringdu.com");
        User teacher = saveUser("my-invitation-teacher@ringdu.com", "010-2000-0006", Role.TEACHER);
        invitationService.createInvitation(academyUserId, invitationRequest(null, teacher.getPhone()));
        invitationService.createInvitation(academyUserId, invitationRequest(null, "010-2000-9998"));

        var responses = invitationService.getMyTeacherInvitations(teacher.getId());

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).teacherPhone()).isEqualTo(teacher.getPhone());
        assertThat(responses.get(0).academyName()).isEqualTo("링듀수학학원");
    }

    @Test
    @DisplayName("TEACHER는 자기 phone 초대장을 수락할 수 있고 AcademyMember가 생성된다")
    void teacherCanAcceptOwnPhoneInvitation() {
        Long academyUserId = approvedAcademyUserId("accept-academy@ringdu.com");
        Long academyId = academyRepository.findByUserId(academyUserId).orElseThrow().getId();
        User teacher = saveUser("accept-teacher@ringdu.com", "010-2000-0007", Role.TEACHER);
        Long invitationId = invitationService.createInvitation(
                academyUserId,
                invitationRequest(null, teacher.getPhone())
        ).invitationId();

        var response = invitationService.acceptInvitation(teacher.getId(), invitationId);

        assertThat(response.status()).isEqualTo(AcademyTeacherInvitationStatus.ACCEPTED);
        assertThat(response.teacherUserId()).isEqualTo(teacher.getId());
        assertThat(academyMemberRepository.existsByAcademyIdAndUserId(academyId, teacher.getId())).isTrue();
        assertThat(invitationRepository.findById(invitationId).orElseThrow().getTeacherUserId())
                .isEqualTo(teacher.getId());
    }

    @Test
    @DisplayName("다른 phone의 TEACHER는 초대장을 수락할 수 없다")
    void cannotAcceptOtherPhoneInvitation() {
        Long academyUserId = approvedAcademyUserId("accept-mismatch-academy@ringdu.com");
        User teacher = saveUser("mismatch-teacher@ringdu.com", "010-2000-0008", Role.TEACHER);
        Long invitationId = invitationService.createInvitation(
                academyUserId,
                invitationRequest(null, "010-2000-9997")
        ).invitationId();

        assertThatThrownBy(() -> invitationService.acceptInvitation(teacher.getId(), invitationId))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.TEACHER_INVITATION_PHONE_MISMATCH);
    }

    @Test
    @DisplayName("TEACHER는 자기 phone 초대장을 거절할 수 있다")
    void teacherCanRejectOwnPhoneInvitation() {
        Long academyUserId = approvedAcademyUserId("reject-academy@ringdu.com");
        User teacher = saveUser("reject-teacher@ringdu.com", "010-2000-0009", Role.TEACHER);
        Long invitationId = invitationService.createInvitation(
                academyUserId,
                invitationRequest(null, teacher.getPhone())
        ).invitationId();

        var response = invitationService.rejectInvitation(teacher.getId(), invitationId);

        assertThat(response.status()).isEqualTo(AcademyTeacherInvitationStatus.REJECTED);
        assertThat(invitationRepository.findById(invitationId).orElseThrow().getStatus())
                .isEqualTo(AcademyTeacherInvitationStatus.REJECTED);
    }

    private Long approvedAcademyUserId(String email) {
        var signupResponse = authService.signupAcademy(academySignupRequest(email));
        adminAcademySignupApplicationService.approve(signupResponse.applicationId(), 1L);
        return userRepository.findByEmail(email).orElseThrow().getId();
    }

    private User saveUser(String email, String phone, Role role) {
        return userRepository.save(User.createLocalUser(
                email,
                passwordEncoder.encode("password1234"),
                "테스트 사용자",
                phone,
                role
        ));
    }

    private TeacherInvitationCreateRequest invitationRequest(Long teacherUserId, String teacherPhone) {
        return new TeacherInvitationCreateRequest(
                teacherUserId,
                teacherPhone,
                "링듀수학학원에 함께해 주세요."
        );
    }

    private AcademySignupRequest academySignupRequest(String email) {
        return new AcademySignupRequest(
                email,
                "password1234",
                "password1234",
                "링듀수학학원",
                "홍길동",
                "010-1000-" + Math.abs(email.hashCode() % 9000 + 1000),
                "06123",
                "서울시 강남구 테헤란로",
                "101호"
        );
    }
}

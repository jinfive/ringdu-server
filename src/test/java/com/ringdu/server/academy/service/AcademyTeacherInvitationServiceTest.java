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
    @DisplayName("ACADEMY는 선생님 초대장을 생성할 수 있다")
    void academyCanCreateInvitation() {
        Long academyUserId = approvedAcademyUserId("invitation-create-academy@ringdu.com");

        var response = invitationService.createInvitation(
                academyUserId,
                invitationRequest("INVITED-TEACHER@RINGDU.COM")
        );

        assertThat(response.invitationId()).isNotNull();
        assertThat(response.teacherEmail()).isEqualTo("invited-teacher@ringdu.com");
        assertThat(response.status()).isEqualTo(AcademyTeacherInvitationStatus.PENDING);
        assertThat(response.expiresAt()).isNotNull();
    }

    @Test
    @DisplayName("message가 비어 있으면 학원명 기반 기본 메시지를 저장한다")
    void blankMessageFallsBackToDefaultMessage() {
        Long academyUserId = approvedAcademyUserId("invitation-default-message-academy@ringdu.com");

        var response = invitationService.createInvitation(
                academyUserId,
                new TeacherInvitationCreateRequest(
                        "default-message-teacher@ringdu.com",
                        "010-2222-3333",
                        " "
                )
        );

        assertThat(response.message()).isEqualTo("""
                링듀수학학원에서 선생님 초대장을 보냈습니다.
                초대를 수락하면 해당 학원의 선생님으로 연결됩니다.""");
    }

    @Test
    @DisplayName("기존 사용자가 TEACHER가 아니면 초대장을 생성할 수 없다")
    void cannotInviteNonTeacherUser() {
        Long academyUserId = approvedAcademyUserId("invitation-non-teacher-academy@ringdu.com");
        saveUser("already-parent@ringdu.com", Role.PARENT);

        assertThatThrownBy(() -> invitationService.createInvitation(
                academyUserId,
                invitationRequest("already-parent@ringdu.com")
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.TEACHER_ROLE_REQUIRED);
    }

    @Test
    @DisplayName("같은 학원과 같은 이메일의 PENDING 초대장은 중복 생성할 수 없다")
    void cannotCreateDuplicatedPendingInvitation() {
        Long academyUserId = approvedAcademyUserId("invitation-duplicate-academy@ringdu.com");
        invitationService.createInvitation(academyUserId, invitationRequest("duplicate-teacher@ringdu.com"));

        assertThatThrownBy(() -> invitationService.createInvitation(
                academyUserId,
                invitationRequest("DUPLICATE-TEACHER@RINGDU.COM")
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.TEACHER_INVITATION_ALREADY_EXISTS);
    }

    @Test
    @DisplayName("이미 같은 학원에 연결된 선생님은 다시 초대할 수 없다")
    void cannotInviteAlreadyConnectedTeacher() {
        Long academyUserId = approvedAcademyUserId("invitation-connected-academy@ringdu.com");
        User teacher = saveUser("connected-teacher@ringdu.com", Role.TEACHER);
        Long invitationId = invitationService.createInvitation(
                academyUserId,
                invitationRequest(teacher.getEmail())
        ).invitationId();
        invitationService.acceptInvitation(teacher.getId(), invitationId);

        assertThatThrownBy(() -> invitationService.createInvitation(
                academyUserId,
                invitationRequest(teacher.getEmail())
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.TEACHER_ALREADY_CONNECTED);
    }

    @Test
    @DisplayName("ACADEMY는 자기 학원의 초대장 목록을 조회할 수 있다")
    void academyCanGetInvitations() {
        Long academyUserId = approvedAcademyUserId("invitation-list-academy@ringdu.com");
        invitationService.createInvitation(academyUserId, invitationRequest("first-list-teacher@ringdu.com"));
        invitationService.createInvitation(academyUserId, invitationRequest("second-list-teacher@ringdu.com"));

        var responses = invitationService.getMyAcademyInvitations(academyUserId);

        assertThat(responses).hasSize(2);
        assertThat(responses)
                .extracting("teacherEmail")
                .containsExactly("second-list-teacher@ringdu.com", "first-list-teacher@ringdu.com");
    }

    @Test
    @DisplayName("ACADEMY는 자기 학원의 소속 선생님 목록을 조회할 수 있다")
    void academyCanGetTeachers() {
        Long academyUserId = approvedAcademyUserId("teacher-list-academy@ringdu.com");
        User teacher = saveUser("listed-teacher@ringdu.com", Role.TEACHER);
        Long invitationId = invitationService.createInvitation(
                academyUserId,
                invitationRequest(teacher.getEmail())
        ).invitationId();
        invitationService.acceptInvitation(teacher.getId(), invitationId);

        var responses = invitationService.getMyAcademyTeachers(academyUserId);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).teacherUserId()).isEqualTo(teacher.getId());
        assertThat(responses.get(0).email()).isEqualTo(teacher.getEmail());
        assertThat(responses.get(0).connectedAt()).isNotNull();
    }

    @Test
    @DisplayName("TEACHER는 자기 이메일로 온 초대장 목록을 조회할 수 있다")
    void teacherCanGetOwnInvitations() {
        Long academyUserId = approvedAcademyUserId("teacher-invitation-list-academy@ringdu.com");
        User teacher = saveUser("my-invitation-teacher@ringdu.com", Role.TEACHER);
        invitationService.createInvitation(academyUserId, invitationRequest(teacher.getEmail()));
        invitationService.createInvitation(academyUserId, invitationRequest("other-invitation-teacher@ringdu.com"));

        var responses = invitationService.getMyTeacherInvitations(teacher.getId());

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).teacherEmail()).isEqualTo(teacher.getEmail());
        assertThat(responses.get(0).academyName()).isEqualTo("링듀수학학원");
    }

    @Test
    @DisplayName("TEACHER는 자기 이메일 초대장을 수락할 수 있고 AcademyMember가 생성된다")
    void teacherCanAcceptOwnInvitation() {
        Long academyUserId = approvedAcademyUserId("accept-academy@ringdu.com");
        Long academyId = academyRepository.findByUserId(academyUserId).orElseThrow().getId();
        User teacher = saveUser("accept-teacher@ringdu.com", Role.TEACHER);
        Long invitationId = invitationService.createInvitation(
                academyUserId,
                invitationRequest(teacher.getEmail())
        ).invitationId();

        var response = invitationService.acceptInvitation(teacher.getId(), invitationId);

        assertThat(response.status()).isEqualTo(AcademyTeacherInvitationStatus.ACCEPTED);
        assertThat(academyMemberRepository.existsByAcademyIdAndUserId(academyId, teacher.getId())).isTrue();
        assertThat(invitationRepository.findById(invitationId).orElseThrow().getStatus())
                .isEqualTo(AcademyTeacherInvitationStatus.ACCEPTED);
    }

    @Test
    @DisplayName("다른 이메일의 초대장은 수락할 수 없다")
    void cannotAcceptOtherEmailInvitation() {
        Long academyUserId = approvedAcademyUserId("accept-mismatch-academy@ringdu.com");
        User teacher = saveUser("mismatch-teacher@ringdu.com", Role.TEACHER);
        Long invitationId = invitationService.createInvitation(
                academyUserId,
                invitationRequest("other-mismatch-teacher@ringdu.com")
        ).invitationId();

        assertThatThrownBy(() -> invitationService.acceptInvitation(teacher.getId(), invitationId))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.TEACHER_INVITATION_EMAIL_MISMATCH);
    }

    @Test
    @DisplayName("이미 처리된 초대장은 다시 수락할 수 없다")
    void cannotAcceptProcessedInvitation() {
        Long academyUserId = approvedAcademyUserId("accept-processed-academy@ringdu.com");
        User teacher = saveUser("processed-teacher@ringdu.com", Role.TEACHER);
        Long invitationId = invitationService.createInvitation(
                academyUserId,
                invitationRequest(teacher.getEmail())
        ).invitationId();
        invitationService.rejectInvitation(teacher.getId(), invitationId);

        assertThatThrownBy(() -> invitationService.acceptInvitation(teacher.getId(), invitationId))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.TEACHER_INVITATION_ALREADY_PROCESSED);
    }

    @Test
    @DisplayName("TEACHER는 자기 이메일 초대장을 거절할 수 있다")
    void teacherCanRejectOwnInvitation() {
        Long academyUserId = approvedAcademyUserId("reject-academy@ringdu.com");
        User teacher = saveUser("reject-teacher@ringdu.com", Role.TEACHER);
        Long invitationId = invitationService.createInvitation(
                academyUserId,
                invitationRequest(teacher.getEmail())
        ).invitationId();

        var response = invitationService.rejectInvitation(teacher.getId(), invitationId);

        assertThat(response.status()).isEqualTo(AcademyTeacherInvitationStatus.REJECTED);
        assertThat(invitationRepository.findById(invitationId).orElseThrow().getStatus())
                .isEqualTo(AcademyTeacherInvitationStatus.REJECTED);
    }

    @Test
    @DisplayName("거절된 초대장은 다시 수락할 수 없다")
    void cannotAcceptRejectedInvitation() {
        Long academyUserId = approvedAcademyUserId("reject-then-accept-academy@ringdu.com");
        User teacher = saveUser("reject-then-accept-teacher@ringdu.com", Role.TEACHER);
        Long invitationId = invitationService.createInvitation(
                academyUserId,
                invitationRequest(teacher.getEmail())
        ).invitationId();
        invitationService.rejectInvitation(teacher.getId(), invitationId);

        assertThatThrownBy(() -> invitationService.acceptInvitation(teacher.getId(), invitationId))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.TEACHER_INVITATION_ALREADY_PROCESSED);
    }

    private Long approvedAcademyUserId(String email) {
        var signupResponse = authService.signupAcademy(academySignupRequest(email));
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

    private TeacherInvitationCreateRequest invitationRequest(String teacherEmail) {
        return new TeacherInvitationCreateRequest(
                teacherEmail,
                "010-2222-3333",
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
                "010-1234-5678",
                "06123",
                "서울시 강남구 테헤란로",
                "101호"
        );
    }
}

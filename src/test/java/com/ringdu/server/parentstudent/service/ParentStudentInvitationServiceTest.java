package com.ringdu.server.parentstudent.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ringdu.server.global.exception.BusinessException;
import com.ringdu.server.global.exception.ErrorCode;
import com.ringdu.server.parentstudent.dto.ParentStudentInvitationCreateRequest;
import com.ringdu.server.parentstudent.dto.StudentParentInvitationCreateRequest;
import com.ringdu.server.parentstudent.entity.ParentStudentInvitationStatus;
import com.ringdu.server.parentstudent.repository.ParentStudentRelationRepository;
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
class ParentStudentInvitationServiceTest {

    @Autowired
    private ParentStudentInvitationService invitationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ParentStudentRelationRepository relationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("PARENT는 STUDENT에게 초대장을 생성할 수 있다")
    void parentCanInviteStudent() {
        User parent = saveUser("parent-invite-student-parent@ringdu.com", Role.PARENT);
        User student = saveUser("parent-invite-student-student@ringdu.com", Role.STUDENT);

        var response = invitationService.createStudentInvitation(parent.getId(), studentInvitationRequest(student.getEmail()));

        assertThat(response.invitationId()).isNotNull();
        assertThat(response.requesterRole()).isEqualTo(Role.PARENT);
        assertThat(response.targetRole()).isEqualTo(Role.STUDENT);
        assertThat(response.receiverEmail()).isEqualTo(student.getEmail());
        assertThat(response.status()).isEqualTo(ParentStudentInvitationStatus.PENDING);
    }

    @Test
    @DisplayName("STUDENT는 PARENT에게 초대장을 생성할 수 있다")
    void studentCanInviteParent() {
        User student = saveUser("student-invite-parent-student@ringdu.com", Role.STUDENT);
        User parent = saveUser("student-invite-parent-parent@ringdu.com", Role.PARENT);

        var response = invitationService.createParentInvitation(student.getId(), parentInvitationRequest(parent.getEmail()));

        assertThat(response.invitationId()).isNotNull();
        assertThat(response.requesterRole()).isEqualTo(Role.STUDENT);
        assertThat(response.targetRole()).isEqualTo(Role.PARENT);
        assertThat(response.receiverEmail()).isEqualTo(parent.getEmail());
        assertThat(response.status()).isEqualTo(ParentStudentInvitationStatus.PENDING);
    }

    @Test
    @DisplayName("PARENT/STUDENT 외 role은 초대장을 생성할 수 없다")
    void nonParentStudentCannotCreateInvitation() {
        User teacher = saveUser("teacher-cannot-parent-student-invite@ringdu.com", Role.TEACHER);

        assertThatThrownBy(() -> invitationService.createStudentInvitation(
                teacher.getId(),
                studentInvitationRequest("blocked-student@ringdu.com")
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PARENT_ROLE_REQUIRED);

        assertThatThrownBy(() -> invitationService.createParentInvitation(
                teacher.getId(),
                parentInvitationRequest("blocked-parent@ringdu.com")
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.STUDENT_ROLE_REQUIRED);
    }

    @Test
    @DisplayName("수신자 email이 현재 로그인 사용자 email과 다르면 수락할 수 없다")
    void cannotAcceptOtherReceiverInvitation() {
        User parent = saveUser("email-mismatch-parent@ringdu.com", Role.PARENT);
        saveUser("email-mismatch-student@ringdu.com", Role.STUDENT);
        User otherStudent = saveUser("other-email-mismatch-student@ringdu.com", Role.STUDENT);
        Long invitationId = invitationService.createStudentInvitation(
                parent.getId(),
                studentInvitationRequest("email-mismatch-student@ringdu.com")
        ).invitationId();

        assertThatThrownBy(() -> invitationService.acceptInvitation(otherStudent.getId(), invitationId))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PARENT_STUDENT_INVITATION_EMAIL_MISMATCH);
    }

    @Test
    @DisplayName("수락 시 ParentStudentRelation이 생성된다")
    void acceptCreatesRelation() {
        User parent = saveUser("accept-parent@ringdu.com", Role.PARENT);
        User student = saveUser("accept-student@ringdu.com", Role.STUDENT);
        Long invitationId = invitationService.createStudentInvitation(
                parent.getId(),
                studentInvitationRequest(student.getEmail())
        ).invitationId();

        var response = invitationService.acceptInvitation(student.getId(), invitationId);

        assertThat(response.status()).isEqualTo(ParentStudentInvitationStatus.ACCEPTED);
        assertThat(relationRepository.existsByParentIdAndStudentId(parent.getId(), student.getId())).isTrue();
    }

    @Test
    @DisplayName("이미 연결된 관계는 중복 생성할 수 없다")
    void cannotCreateDuplicatedRelation() {
        User parent = saveUser("duplicate-relation-parent@ringdu.com", Role.PARENT);
        User student = saveUser("duplicate-relation-student@ringdu.com", Role.STUDENT);
        Long firstInvitationId = invitationService.createStudentInvitation(
                parent.getId(),
                studentInvitationRequest(student.getEmail())
        ).invitationId();
        invitationService.acceptInvitation(student.getId(), firstInvitationId);

        assertThatThrownBy(() -> invitationService.createStudentInvitation(
                parent.getId(),
                studentInvitationRequest(student.getEmail())
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PARENT_STUDENT_ALREADY_CONNECTED);
    }

    @Test
    @DisplayName("거절 시 관계가 생성되지 않는다")
    void rejectDoesNotCreateRelation() {
        User student = saveUser("reject-student@ringdu.com", Role.STUDENT);
        User parent = saveUser("reject-parent@ringdu.com", Role.PARENT);
        Long invitationId = invitationService.createParentInvitation(
                student.getId(),
                parentInvitationRequest(parent.getEmail())
        ).invitationId();

        var response = invitationService.rejectInvitation(parent.getId(), invitationId);

        assertThat(response.status()).isEqualTo(ParentStudentInvitationStatus.REJECTED);
        assertThat(relationRepository.existsByParentIdAndStudentId(parent.getId(), student.getId())).isFalse();
    }

    @Test
    @DisplayName("처리된 초대장은 재수락/재거절할 수 없다")
    void cannotProcessInvitationTwice() {
        User parent = saveUser("processed-parent@ringdu.com", Role.PARENT);
        User student = saveUser("processed-student@ringdu.com", Role.STUDENT);
        Long invitationId = invitationService.createStudentInvitation(
                parent.getId(),
                studentInvitationRequest(student.getEmail())
        ).invitationId();
        invitationService.rejectInvitation(student.getId(), invitationId);

        assertThatThrownBy(() -> invitationService.acceptInvitation(student.getId(), invitationId))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PARENT_STUDENT_INVITATION_ALREADY_PROCESSED);

        assertThatThrownBy(() -> invitationService.rejectInvitation(student.getId(), invitationId))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PARENT_STUDENT_INVITATION_ALREADY_PROCESSED);
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

    private ParentStudentInvitationCreateRequest studentInvitationRequest(String studentEmail) {
        return new ParentStudentInvitationCreateRequest(
                studentEmail,
                "010-2222-3333",
                "자녀 연결 요청입니다."
        );
    }

    private StudentParentInvitationCreateRequest parentInvitationRequest(String parentEmail) {
        return new StudentParentInvitationCreateRequest(
                parentEmail,
                "010-3333-4444",
                "보호자 연결 요청입니다."
        );
    }
}

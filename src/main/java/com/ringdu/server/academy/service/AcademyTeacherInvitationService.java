package com.ringdu.server.academy.service;

import com.ringdu.server.academy.dto.AcademyTeacherResponse;
import com.ringdu.server.academy.dto.MyTeacherInvitationResponse;
import com.ringdu.server.academy.dto.TeacherInvitationCreateRequest;
import com.ringdu.server.academy.dto.TeacherInvitationResponse;
import com.ringdu.server.academy.entity.Academy;
import com.ringdu.server.academy.entity.AcademyMember;
import com.ringdu.server.academy.entity.AcademyTeacherInvitation;
import com.ringdu.server.academy.entity.AcademyTeacherInvitationStatus;
import com.ringdu.server.academy.repository.AcademyMemberRepository;
import com.ringdu.server.academy.repository.AcademyRepository;
import com.ringdu.server.academy.repository.AcademyTeacherInvitationRepository;
import com.ringdu.server.global.exception.BusinessException;
import com.ringdu.server.global.exception.ErrorCode;
import com.ringdu.server.user.entity.Role;
import com.ringdu.server.user.entity.User;
import com.ringdu.server.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AcademyTeacherInvitationService {

    private static final int INVITATION_EXPIRATION_DAYS = 14;

    private final AcademyRepository academyRepository;
    private final AcademyTeacherInvitationRepository invitationRepository;
    private final AcademyMemberRepository academyMemberRepository;
    private final UserRepository userRepository;

    @Transactional
    public TeacherInvitationResponse createInvitation(Long academyUserId, TeacherInvitationCreateRequest request) {
        Academy academy = getAcademyForUser(academyUserId);
        String teacherPhone = normalizePhone(request.teacherPhone());
        User teacher = resolveTeacher(request.teacherUserId(), teacherPhone);

        if (teacher != null) {
            validateTeacherNotConnected(academy.getId(), teacher.getId());
        }
        validateNoPendingInvitation(academy.getId(), teacherPhone, teacher);
        validateNoAcceptedInvitation(academy.getId(), teacherPhone, teacher);

        AcademyTeacherInvitation invitation = AcademyTeacherInvitation.create(
                academy,
                teacher == null ? null : teacher.getId(),
                teacher == null ? null : teacher.getEmail(),
                teacherPhone,
                normalizeMessage(request.message(), academy.getName()),
                academyUserId,
                LocalDateTime.now().plusDays(INVITATION_EXPIRATION_DAYS)
        );

        return TeacherInvitationResponse.from(invitationRepository.save(invitation));
    }

    @Transactional(readOnly = true)
    public List<TeacherInvitationResponse> getMyAcademyInvitations(Long academyUserId) {
        Academy academy = getAcademyForUser(academyUserId);
        return invitationRepository.findAllByAcademyIdOrderByCreatedAtDesc(academy.getId())
                .stream()
                .map(TeacherInvitationResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AcademyTeacherResponse> getMyAcademyTeachers(Long academyUserId) {
        Academy academy = getAcademyForUser(academyUserId);
        return academyMemberRepository.findAllByAcademyIdOrderByCreatedAtDesc(academy.getId())
                .stream()
                .map(AcademyTeacherResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MyTeacherInvitationResponse> getMyTeacherInvitations(Long teacherUserId) {
        User teacher = getTeacherUser(teacherUserId);
        String teacherPhone = normalizePhone(teacher.getPhone());

        return invitationRepository.findAllByTeacherPhoneOrderByCreatedAtDesc(teacherPhone)
                .stream()
                .sorted(Comparator
                        .comparing((AcademyTeacherInvitation invitation) -> !invitation.isPending())
                        .thenComparing(AcademyTeacherInvitation::getCreatedAt, Comparator.reverseOrder()))
                .map(MyTeacherInvitationResponse::from)
                .toList();
    }

    @Transactional
    public MyTeacherInvitationResponse acceptInvitation(Long teacherUserId, Long invitationId) {
        User teacher = getTeacherUser(teacherUserId);
        AcademyTeacherInvitation invitation = getInvitation(invitationId);
        validateInvitationBelongsToTeacher(invitation, teacher);
        validateTeacherNotConnected(invitation.getAcademy().getId(), teacher.getId());

        academyMemberRepository.save(AcademyMember.createTeacher(invitation.getAcademy(), teacher));
        invitation.accept(teacherUserId, LocalDateTime.now());
        return MyTeacherInvitationResponse.from(invitation);
    }

    @Transactional
    public MyTeacherInvitationResponse rejectInvitation(Long teacherUserId, Long invitationId) {
        User teacher = getTeacherUser(teacherUserId);
        AcademyTeacherInvitation invitation = getInvitation(invitationId);
        validateInvitationBelongsToTeacher(invitation, teacher);

        invitation.reject(teacherUserId, LocalDateTime.now());
        return MyTeacherInvitationResponse.from(invitation);
    }

    private Academy getAcademyForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        if (user.getRole() != Role.ACADEMY) {
            throw new BusinessException(ErrorCode.ACADEMY_ACCESS_DENIED);
        }

        return academyRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ACADEMY_NOT_FOUND));
    }

    private User getTeacherUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        validateTeacherRole(user);
        return user;
    }

    private AcademyTeacherInvitation getInvitation(Long invitationId) {
        return invitationRepository.findById(invitationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TEACHER_INVITATION_NOT_FOUND));
    }

    private void validateTeacherRole(User user) {
        if (user.getRole() != Role.TEACHER) {
            throw new BusinessException(ErrorCode.TEACHER_ROLE_REQUIRED);
        }
    }

    private void validateActiveTeacher(User user) {
        validateTeacherRole(user);
        if (user.getStatus() != com.ringdu.server.user.entity.UserStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.TEACHER_ROLE_REQUIRED);
        }
    }

    private void validateTeacherNotConnected(Long academyId, Long teacherUserId) {
        if (academyMemberRepository.existsByAcademyIdAndUserId(academyId, teacherUserId)) {
            throw new BusinessException(ErrorCode.TEACHER_ALREADY_CONNECTED);
        }
    }

    private void validateNoPendingInvitation(Long academyId, String teacherPhone, User teacher) {
        if (invitationRepository.existsByAcademyIdAndTeacherPhoneAndStatus(
                academyId, teacherPhone, AcademyTeacherInvitationStatus.PENDING
        ) || (teacher != null && invitationRepository.existsByAcademyIdAndTeacherUserIdAndStatus(
                academyId, teacher.getId(), AcademyTeacherInvitationStatus.PENDING
        ))) {
            throw new BusinessException(ErrorCode.TEACHER_INVITATION_ALREADY_EXISTS);
        }
    }

    private void validateNoAcceptedInvitation(Long academyId, String teacherPhone, User teacher) {
        if (invitationRepository.existsByAcademyIdAndTeacherPhoneAndStatus(
                academyId, teacherPhone, AcademyTeacherInvitationStatus.ACCEPTED
        ) || (teacher != null && invitationRepository.existsByAcademyIdAndTeacherUserIdAndStatus(
                academyId, teacher.getId(), AcademyTeacherInvitationStatus.ACCEPTED
        ))) {
            throw new BusinessException(ErrorCode.TEACHER_ALREADY_CONNECTED);
        }
    }

    private void validateInvitationBelongsToTeacher(AcademyTeacherInvitation invitation, User teacher) {
        if (!invitation.getTeacherPhone().equals(normalizePhone(teacher.getPhone()))) {
            throw new BusinessException(ErrorCode.TEACHER_INVITATION_PHONE_MISMATCH);
        }
    }

    private User resolveTeacher(Long teacherUserId, String teacherPhone) {
        if (teacherUserId != null) {
            User teacher = userRepository.findById(teacherUserId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
            validateActiveTeacher(teacher);
            if (!teacherPhone.equals(normalizePhone(teacher.getPhone()))) {
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
            }
            return teacher;
        }

        List<User> teachers = userRepository.findAllByPhoneAndRoleAndStatus(
                teacherPhone,
                Role.TEACHER,
                com.ringdu.server.user.entity.UserStatus.ACTIVE
        );
        return teachers.size() == 1 ? teachers.get(0) : null;
    }

    private String normalizePhone(String phone) {
        if (!StringUtils.hasText(phone)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
        return phone.trim();
    }

    private String normalizeMessage(String message, String academyName) {
        if (message == null || message.isBlank()) {
            return defaultInvitationMessage(academyName);
        }
        return message.trim();
    }

    private String defaultInvitationMessage(String academyName) {
        return academyName + "에서 선생님 초대장을 보냈습니다.\n"
                + "초대를 수락하면 해당 학원의 선생님으로 연결됩니다.";
    }
}

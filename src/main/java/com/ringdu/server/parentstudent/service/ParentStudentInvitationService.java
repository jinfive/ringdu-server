package com.ringdu.server.parentstudent.service;

import com.ringdu.server.global.exception.BusinessException;
import com.ringdu.server.global.exception.ErrorCode;
import com.ringdu.server.parentstudent.dto.ParentStudentInvitationCreateRequest;
import com.ringdu.server.parentstudent.dto.ParentStudentInvitationResponse;
import com.ringdu.server.parentstudent.dto.ParentStudentRelationResponse;
import com.ringdu.server.parentstudent.dto.StudentParentInvitationCreateRequest;
import com.ringdu.server.parentstudent.entity.ParentStudentInvitation;
import com.ringdu.server.parentstudent.entity.ParentStudentInvitationStatus;
import com.ringdu.server.parentstudent.entity.ParentStudentRelation;
import com.ringdu.server.parentstudent.repository.ParentStudentInvitationRepository;
import com.ringdu.server.parentstudent.repository.ParentStudentRelationRepository;
import com.ringdu.server.user.entity.Role;
import com.ringdu.server.user.entity.User;
import com.ringdu.server.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ParentStudentInvitationService {

    private static final int INVITATION_EXPIRATION_DAYS = 14;

    private final ParentStudentInvitationRepository invitationRepository;
    private final ParentStudentRelationRepository relationRepository;
    private final UserRepository userRepository;

    @Transactional
    public ParentStudentInvitationResponse createStudentInvitation(
            Long parentUserId,
            ParentStudentInvitationCreateRequest request
    ) {
        User parent = getUserWithRole(parentUserId, Role.PARENT);
        String studentEmail = normalizeEmail(request.studentEmail());
        Long studentUserId = userRepository.findByEmail(studentEmail)
                .map(student -> {
                    validateRole(student, Role.STUDENT);
                    validateNotConnected(parent.getId(), student.getId());
                    return student.getId();
                })
                .orElse(null);

        validateNoPendingInvitation(parent.getId(), studentEmail);
        ParentStudentInvitation invitation = ParentStudentInvitation.create(
                parent.getId(),
                studentEmail,
                request.studentPhone().trim(),
                Role.PARENT,
                Role.STUDENT,
                studentUserId,
                parent.getId(),
                normalizeMessage(request.message(), "자녀 연결 요청입니다."),
                LocalDateTime.now().plusDays(INVITATION_EXPIRATION_DAYS)
        );

        return toResponse(invitationRepository.save(invitation), parentUserId);
    }

    @Transactional
    public ParentStudentInvitationResponse createParentInvitation(
            Long studentUserId,
            StudentParentInvitationCreateRequest request
    ) {
        User student = getUserWithRole(studentUserId, Role.STUDENT);
        String parentEmail = normalizeEmail(request.parentEmail());
        Long parentUserId = userRepository.findByEmail(parentEmail)
                .map(parent -> {
                    validateRole(parent, Role.PARENT);
                    validateNotConnected(parent.getId(), student.getId());
                    return parent.getId();
                })
                .orElse(null);

        validateNoPendingInvitation(student.getId(), parentEmail);
        ParentStudentInvitation invitation = ParentStudentInvitation.create(
                student.getId(),
                parentEmail,
                request.parentPhone().trim(),
                Role.STUDENT,
                Role.PARENT,
                student.getId(),
                parentUserId,
                normalizeMessage(request.message(), "보호자 연결 요청입니다."),
                LocalDateTime.now().plusDays(INVITATION_EXPIRATION_DAYS)
        );

        return toResponse(invitationRepository.save(invitation), studentUserId);
    }

    @Transactional(readOnly = true)
    public List<ParentStudentInvitationResponse> getMyInvitations(Long userId, Role role) {
        User user = getUserWithRole(userId, role);
        String email = normalizeEmail(user.getEmail());

        List<ParentStudentInvitation> received = invitationRepository.findAllByReceiverEmailOrderByCreatedAtDesc(email);
        List<ParentStudentInvitation> sent = invitationRepository.findAllByRequesterUserIdOrderByCreatedAtDesc(userId);

        return java.util.stream.Stream.concat(received.stream(), sent.stream())
                .distinct()
                .sorted(Comparator
                        .comparing((ParentStudentInvitation invitation) -> !invitation.isPending())
                        .thenComparing(ParentStudentInvitation::getCreatedAt, Comparator.reverseOrder()))
                .map(invitation -> toResponse(invitation, userId))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ParentStudentRelationResponse> getMyChildren(Long parentUserId) {
        getUserWithRole(parentUserId, Role.PARENT);
        return relationRepository.findAllByParentIdOrderByCreatedAtDesc(parentUserId)
                .stream()
                .map(ParentStudentRelationResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ParentStudentRelationResponse> getMyParents(Long studentUserId) {
        getUserWithRole(studentUserId, Role.STUDENT);
        return relationRepository.findAllByStudentIdOrderByCreatedAtDesc(studentUserId)
                .stream()
                .map(ParentStudentRelationResponse::from)
                .toList();
    }

    @Transactional
    public ParentStudentInvitationResponse acceptInvitation(Long receiverUserId, Long invitationId) {
        User receiver = userRepository.findById(receiverUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        ParentStudentInvitation invitation = getInvitation(invitationId);
        validateReceiver(invitation, receiver);

        User requester = getUserWithRole(invitation.getRequesterUserId(), invitation.getRequesterRole());
        Long parentUserId = invitation.getRequesterRole() == Role.PARENT ? requester.getId() : receiver.getId();
        Long studentUserId = invitation.getRequesterRole() == Role.STUDENT ? requester.getId() : receiver.getId();
        User parent = getUserWithRole(parentUserId, Role.PARENT);
        User student = getUserWithRole(studentUserId, Role.STUDENT);

        validateNotConnected(parent.getId(), student.getId());
        relationRepository.save(ParentStudentRelation.create(parent, student));
        invitation.accept(receiver.getId(), parent.getId(), student.getId(), LocalDateTime.now());

        return toResponse(invitation, receiverUserId);
    }

    @Transactional
    public ParentStudentInvitationResponse rejectInvitation(Long receiverUserId, Long invitationId) {
        User receiver = userRepository.findById(receiverUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        ParentStudentInvitation invitation = getInvitation(invitationId);
        validateReceiver(invitation, receiver);

        invitation.reject(receiver.getId(), LocalDateTime.now());
        return toResponse(invitation, receiverUserId);
    }

    private ParentStudentInvitation getInvitation(Long invitationId) {
        return invitationRepository.findById(invitationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PARENT_STUDENT_INVITATION_NOT_FOUND));
    }

    private User getUserWithRole(Long userId, Role role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        validateRole(user, role);
        return user;
    }

    private void validateRole(User user, Role role) {
        if (user.getRole() != role) {
            throw new BusinessException(role == Role.PARENT
                    ? ErrorCode.PARENT_ROLE_REQUIRED
                    : ErrorCode.STUDENT_ROLE_REQUIRED);
        }
    }

    private void validateReceiver(ParentStudentInvitation invitation, User receiver) {
        validateRole(receiver, invitation.getTargetRole());
        if (!invitation.getReceiverEmail().equals(normalizeEmail(receiver.getEmail()))) {
            throw new BusinessException(ErrorCode.PARENT_STUDENT_INVITATION_EMAIL_MISMATCH);
        }
    }

    private void validateNotConnected(Long parentUserId, Long studentUserId) {
        if (relationRepository.existsByParentIdAndStudentId(parentUserId, studentUserId)) {
            throw new BusinessException(ErrorCode.PARENT_STUDENT_ALREADY_CONNECTED);
        }
    }

    private void validateNoPendingInvitation(Long requesterUserId, String receiverEmail) {
        if (invitationRepository.existsByRequesterUserIdAndReceiverEmailAndStatus(
                requesterUserId,
                receiverEmail,
                ParentStudentInvitationStatus.PENDING
        )) {
            throw new BusinessException(ErrorCode.PARENT_STUDENT_INVITATION_ALREADY_EXISTS);
        }
    }

    private ParentStudentInvitationResponse toResponse(ParentStudentInvitation invitation, Long currentUserId) {
        String requesterName = userRepository.findById(invitation.getRequesterUserId())
                .map(User::getName)
                .orElse("알 수 없음");
        return ParentStudentInvitationResponse.from(invitation, requesterName, currentUserId);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeMessage(String message, String defaultMessage) {
        if (message == null || message.isBlank()) {
            return defaultMessage;
        }
        return message.trim();
    }
}

package com.ringdu.server.academy.service;

import com.ringdu.server.academy.dto.AcademyStudentInvitationCreateRequest;
import com.ringdu.server.academy.dto.AcademyStudentInvitationResponse;
import com.ringdu.server.academy.entity.Academy;
import com.ringdu.server.academy.entity.AcademyStudentInvitation;
import com.ringdu.server.academy.entity.AcademyStudentInvitationStatus;
import com.ringdu.server.academy.repository.AcademyRepository;
import com.ringdu.server.academy.repository.AcademyStudentInvitationRepository;
import com.ringdu.server.global.exception.BusinessException;
import com.ringdu.server.global.exception.ErrorCode;
import com.ringdu.server.student.entity.StudentProfile;
import com.ringdu.server.student.repository.StudentProfileRepository;
import com.ringdu.server.user.entity.User;
import com.ringdu.server.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AcademyStudentInvitationService {

    private final AcademyStudentInvitationRepository invitationRepository;
    private final AcademyRepository academyRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final UserRepository userRepository;

    @Transactional
    public AcademyStudentInvitationResponse createInvitation(Long academyUserId, AcademyStudentInvitationCreateRequest request) {
        Academy academy = academyRepository.findByUserId(academyUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ACADEMY_NOT_FOUND));

        User receiver = userRepository.findById(request.receiverUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // Check if already invited (PENDING)
        if (invitationRepository.existsByAcademyIdAndReceiverUserIdAndStatus(
                academy.getId(), receiver.getId(), AcademyStudentInvitationStatus.PENDING)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "이미 대기 중인 초대장이 있습니다.");
        }

        // Check if already linked
        if (studentProfileRepository.existsByAcademyIdAndUserId(academy.getId(), receiver.getId())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "이미 학원 학생으로 등록된 계정입니다.");
        }

        AcademyStudentInvitation invitation = AcademyStudentInvitation.builder()
                .academy(academy)
                .studentProfileId(request.studentProfileId())
                .receiverUserId(receiver.getId())
                .receiverEmail(receiver.getEmail())
                .receiverPhone(receiver.getPhone())
                .message(request.message())
                .createdByUserId(academyUserId)
                .build();

        return toResponse(invitationRepository.save(invitation));
    }

    @Transactional(readOnly = true)
    public List<AcademyStudentInvitationResponse> getAcademyInvitations(Long academyUserId) {
        Academy academy = academyRepository.findByUserId(academyUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ACADEMY_NOT_FOUND));
        return invitationRepository.findAllByAcademyId(academy.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AcademyStudentInvitationResponse> getStudentInvitations(Long studentUserId) {
        return invitationRepository.findAllByReceiverUserId(studentUserId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void acceptInvitation(Long studentUserId, Long invitationId) {
        AcademyStudentInvitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "초대장을 찾을 수 없습니다."));

        if (!invitation.getReceiverUserId().equals(studentUserId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        invitation.accept(studentUserId);

        // Link to StudentProfile
        if (invitation.getStudentProfileId() != null) {
            studentProfileRepository.findById(invitation.getStudentProfileId())
                    .ifPresent(profile -> profile.updateUserId(studentUserId));
        } else {
            // Create new StudentProfile if not exists
            User student = userRepository.findById(studentUserId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
            
            StudentProfile profile = StudentProfile.builder()
                    .academyId(invitation.getAcademy().getId())
                    .userId(studentUserId)
                    .name(student.getName())
                    .email(student.getEmail())
                    .phone(student.getPhone())
                    .status(com.ringdu.server.student.entity.StudentStatus.ACTIVE)
                    .build();
            studentProfileRepository.save(profile);
        }
    }

    @Transactional
    public void rejectInvitation(Long studentUserId, Long invitationId) {
        AcademyStudentInvitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "초대장을 찾을 수 없습니다."));

        if (!invitation.getReceiverUserId().equals(studentUserId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        invitation.reject(studentUserId);
    }

    private AcademyStudentInvitationResponse toResponse(AcademyStudentInvitation invitation) {
        return new AcademyStudentInvitationResponse(
                invitation.getId(),
                invitation.getAcademy().getId(),
                invitation.getAcademy().getName(),
                invitation.getStudentProfileId(),
                invitation.getReceiverUserId(),
                invitation.getReceiverEmail(),
                invitation.getReceiverPhone(),
                invitation.getMessage(),
                invitation.getStatus(),
                invitation.getCreatedByUserId(),
                invitation.getRespondedByUserId(),
                invitation.getRespondedAt(),
                invitation.getCreatedAt(),
                invitation.getUpdatedAt()
        );
    }
}

package com.ringdu.server.student.service;

import com.ringdu.server.global.exception.BusinessException;
import com.ringdu.server.global.exception.ErrorCode;
import com.ringdu.server.parentstudent.entity.ParentStudentRelation;
import com.ringdu.server.parentstudent.entity.ParentStudentRelationStatus;
import com.ringdu.server.parentstudent.repository.ParentStudentRelationRepository;
import com.ringdu.server.student.entity.StudentGuardianAccountLink;
import com.ringdu.server.student.entity.StudentProfile;
import com.ringdu.server.student.repository.StudentGuardianAccountLinkRepository;
import com.ringdu.server.user.entity.Role;
import com.ringdu.server.user.entity.User;
import com.ringdu.server.user.entity.UserStatus;
import com.ringdu.server.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentGuardianLinkService {

    private final StudentGuardianAccountLinkRepository guardianAccountLinkRepository;
    private final ParentStudentRelationRepository parentStudentRelationRepository;
    private final UserRepository userRepository;

    public User resolveRegistrationGuardianParent(String guardianPhone, Long guardianParentUserId) {
        if (!StringUtils.hasText(guardianPhone)) {
            return null;
        }

        List<User> parents = userRepository.findAllByPhoneAndRoleAndStatus(
                guardianPhone.trim(),
                Role.PARENT,
                UserStatus.ACTIVE
        );

        if (guardianParentUserId != null) {
            return parents.stream()
                    .filter(parent -> parent.getId().equals(guardianParentUserId))
                    .findFirst()
                    .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT_VALUE));
        }

        return parents.size() == 1 ? parents.get(0) : null;
    }

    public void linkRegistrationGuardian(StudentProfile profile, User guardianParent) {
        if (profile.getId() == null || guardianParent == null) {
            return;
        }

        if (!guardianAccountLinkRepository.existsByStudentProfileIdAndParentId(profile.getId(), guardianParent.getId())) {
            guardianAccountLinkRepository.save(StudentGuardianAccountLink.create(profile.getId(), guardianParent));
        }
    }

    public User findGuardianParent(StudentProfile profile) {
        if (profile.getId() == null) {
            return null;
        }

        if (profile.getUserId() != null) {
            return parentStudentRelationRepository.findAllByStudentIdAndStatusWithParent(
                            profile.getUserId(),
                            ParentStudentRelationStatus.ACTIVE
                    ).stream()
                    .findFirst()
                    .map(ParentStudentRelation::getParent)
                    .orElseGet(() -> findStudentProfileGuardianParent(profile));
        }

        return findStudentProfileGuardianParent(profile);
    }

    private User findStudentProfileGuardianParent(StudentProfile profile) {
        return guardianAccountLinkRepository.findByStudentProfileIdWithParent(profile.getId())
                .map(StudentGuardianAccountLink::getParent)
                .orElse(null);
    }
}

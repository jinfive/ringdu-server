package com.ringdu.server.student.service;

import com.ringdu.server.student.dto.AcademyStudentResponse;
import com.ringdu.server.student.entity.StudentProfile;
import com.ringdu.server.user.entity.Role;
import com.ringdu.server.user.entity.User;
import com.ringdu.server.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class StudentDetailAssembler {

    private final UserRepository userRepository;
    private final StudentGuardianLinkService guardianLinkService;

    public AcademyStudentResponse toListResponse(StudentProfile profile) {
        return AcademyStudentResponse.of(profile, checkMatchedStudentUserExists(profile.getEmail()));
    }

    public AcademyStudentResponse toDetailResponse(StudentProfile profile) {
        return AcademyStudentResponse.of(
                profile,
                checkMatchedStudentUserExists(profile.getEmail()),
                guardianLinkService.findGuardianParent(profile)
        );
    }

    public AcademyStudentResponse toDetailResponse(StudentProfile profile, User guardianParent) {
        return AcademyStudentResponse.of(
                profile,
                checkMatchedStudentUserExists(profile.getEmail()),
                guardianParent
        );
    }

    private boolean checkMatchedStudentUserExists(String email) {
        if (!StringUtils.hasText(email)) {
            return false;
        }
        return userRepository.findByEmail(email)
                .map(user -> user.getRole() == Role.STUDENT)
                .orElse(false);
    }
}

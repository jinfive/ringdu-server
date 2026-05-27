package com.ringdu.server.student.dto;

import com.ringdu.server.student.entity.StudentProfile;
import com.ringdu.server.student.entity.StudentStatus;
import com.ringdu.server.user.entity.User;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record AcademyStudentResponse(
        Long id,
        Long academyId,
        Long userId,
        String name,
        LocalDate birthDate,
        String school,
        String grade,
        String email,
        String phone,
        String guardianPhone,
        boolean guardianAccountLinked,
        Long guardianParentUserId,
        String guardianParentName,
        String guardianParentEmail,
        String guardianParentPhone,
        StudentStatus status,
        String memo,
        boolean matchedStudentUserExists,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static AcademyStudentResponse of(StudentProfile profile, boolean matchedStudentUserExists) {
        return of(profile, matchedStudentUserExists, null);
    }

    public static AcademyStudentResponse of(
            StudentProfile profile,
            boolean matchedStudentUserExists,
            User guardianParent
    ) {
        return new AcademyStudentResponse(
                profile.getId(),
                profile.getAcademyId(),
                profile.getUserId(),
                profile.getName(),
                profile.getBirthDate(),
                profile.getSchool(),
                profile.getGrade(),
                profile.getEmail(),
                profile.getPhone(),
                resolveGuardianPhone(profile, guardianParent),
                guardianParent != null,
                guardianParent == null ? null : guardianParent.getId(),
                guardianParent == null ? null : guardianParent.getName(),
                guardianParent == null ? null : guardianParent.getEmail(),
                guardianParent == null ? null : guardianParent.getPhone(),
                profile.getStatus(),
                profile.getMemo(),
                matchedStudentUserExists,
                profile.getCreatedAt(),
                profile.getUpdatedAt()
        );
    }

    private static String resolveGuardianPhone(StudentProfile profile, User guardianParent) {
        if (guardianParent != null && guardianParent.getPhone() != null) {
            return guardianParent.getPhone();
        }
        return profile.getGuardianPhone();
    }
}

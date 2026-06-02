package com.ringdu.server.parentstudent.dto;

import com.ringdu.server.student.entity.StudentProfile;

public record ParentStudentProfileResponse(
        Long studentProfileId,
        String studentName,
        Long academyId
) {

    public static ParentStudentProfileResponse from(StudentProfile profile) {
        return new ParentStudentProfileResponse(
                profile.getId(),
                profile.getName(),
                profile.getAcademyId()
        );
    }
}

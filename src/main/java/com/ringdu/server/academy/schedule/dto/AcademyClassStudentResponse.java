package com.ringdu.server.academy.schedule.dto;

import com.ringdu.server.student.entity.StudentProfile;

public record AcademyClassStudentResponse(
        Long studentProfileId,
        String name,
        String school,
        String grade
) {

    public static AcademyClassStudentResponse from(StudentProfile studentProfile) {
        return new AcademyClassStudentResponse(
                studentProfile.getId(),
                studentProfile.getName(),
                studentProfile.getSchool(),
                studentProfile.getGrade()
        );
    }
}

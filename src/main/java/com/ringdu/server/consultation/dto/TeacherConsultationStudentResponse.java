package com.ringdu.server.consultation.dto;

import com.ringdu.server.student.entity.StudentProfile;

public record TeacherConsultationStudentResponse(
        Long studentProfileId,
        String studentName,
        Long academyId,
        String academyName
) {
    public static TeacherConsultationStudentResponse of(StudentProfile studentProfile, String academyName) {
        return new TeacherConsultationStudentResponse(
                studentProfile.getId(),
                studentProfile.getName(),
                studentProfile.getAcademyId(),
                academyName
        );
    }
}

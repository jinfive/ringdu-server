package com.ringdu.server.consultation.dto;

import com.ringdu.server.student.entity.StudentProfile;

public record TeacherConsultationStudentResponse(
        Long studentProfileId,
        String studentName,
        Long academyId
) {
    public static TeacherConsultationStudentResponse of(StudentProfile studentProfile) {
        return new TeacherConsultationStudentResponse(
                studentProfile.getId(),
                studentProfile.getName(),
                studentProfile.getAcademyId()
        );
    }
}

package com.ringdu.server.consultation.dto;

import java.util.List;

public record ParentConsultationOptionResponse(
        Long studentProfileId,
        String studentName,
        Long academyId,
        String academyName,
        List<TeacherOption> teachers
) {

    public record TeacherOption(
            Long teacherUserId,
            String teacherName,
            Long classId,
            String className
    ) {
    }
}

package com.ringdu.server.consultation.dto;

import java.util.List;

public record ParentConsultationOptionResponse(
        Long studentProfileId,
        String studentName,
        Long academyId,
        String academyName,
        List<ConsultantOption> consultants,
        List<TeacherOption> teachers
) {

    public record ConsultantOption(
            com.ringdu.server.consultation.entity.ConsultationConsultantType consultantType,
            Long teacherUserId,
            String consultantName,
            String description,
            List<String> classNames,
            boolean available
    ) {
    }

    public record TeacherOption(
            Long teacherUserId,
            String teacherName,
            List<String> classNames,
            boolean available
    ) {
    }
}

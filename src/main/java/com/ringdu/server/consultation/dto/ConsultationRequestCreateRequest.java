package com.ringdu.server.consultation.dto;

import com.ringdu.server.consultation.entity.ConsultationTopic;
import com.ringdu.server.consultation.entity.ConsultationConsultantType;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

public record ConsultationRequestCreateRequest(
        @NotNull Long academyId,
        @NotNull Long studentProfileId,
        ConsultationConsultantType consultantType,
        Long teacherUserId,
        @NotNull LocalDate requestedDate,
        @NotNull LocalTime requestedStartTime,
        @NotNull LocalTime requestedEndTime,
        @NotNull ConsultationTopic topic,
        String content
) {
    public ConsultationRequestCreateRequest(
            Long academyId,
            Long studentProfileId,
            Long teacherUserId,
            LocalDate requestedDate,
            LocalTime requestedStartTime,
            LocalTime requestedEndTime,
            ConsultationTopic topic,
            String content
    ) {
        this(
                academyId,
                studentProfileId,
                teacherUserId == null ? ConsultationConsultantType.ACADEMY_ACCOUNT : ConsultationConsultantType.TEACHER,
                teacherUserId,
                requestedDate,
                requestedStartTime,
                requestedEndTime,
                topic,
                content
        );
    }
}

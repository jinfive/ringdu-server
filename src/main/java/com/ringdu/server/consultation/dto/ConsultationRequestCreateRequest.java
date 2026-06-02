package com.ringdu.server.consultation.dto;

import com.ringdu.server.consultation.entity.ConsultationTopic;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

public record ConsultationRequestCreateRequest(
        @NotNull Long academyId,
        @NotNull Long studentProfileId,
        Long teacherUserId,
        @NotNull LocalDate requestedDate,
        @NotNull LocalTime requestedStartTime,
        @NotNull LocalTime requestedEndTime,
        @NotNull ConsultationTopic topic,
        String content
) {
}

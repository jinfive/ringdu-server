package com.ringdu.server.consultation.dto;

import com.ringdu.server.consultation.entity.ConsultationType;
import jakarta.validation.constraints.NotNull;
import java.time.DayOfWeek;
import java.time.LocalTime;

public record ConsultationAvailabilityRequest(
        Long academyId,
        Long teacherUserId,
        @NotNull DayOfWeek dayOfWeek,
        @NotNull LocalTime startTime,
        @NotNull LocalTime endTime,
        @NotNull ConsultationType consultationType
) {
}

package com.ringdu.server.consultation.dto;

import com.ringdu.server.consultation.entity.ConsultationType;
import com.ringdu.server.consultation.entity.ConsultationConsultantType;
import jakarta.validation.constraints.NotNull;
import java.time.DayOfWeek;
import java.time.LocalTime;

public record ConsultationAvailabilityRequest(
        Long academyId,
        ConsultationConsultantType consultantType,
        Long teacherUserId,
        @NotNull DayOfWeek dayOfWeek,
        @NotNull LocalTime startTime,
        @NotNull LocalTime endTime,
        @NotNull ConsultationType consultationType
) {
    public ConsultationAvailabilityRequest(
            Long academyId,
            Long teacherUserId,
            DayOfWeek dayOfWeek,
            LocalTime startTime,
            LocalTime endTime,
            ConsultationType consultationType
    ) {
        this(
                academyId,
                teacherUserId == null ? null : ConsultationConsultantType.TEACHER,
                teacherUserId,
                dayOfWeek,
                startTime,
                endTime,
                consultationType
        );
    }
}

package com.ringdu.server.consultation.dto;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record ParentConsultationDateAvailabilityResponse(
        LocalDate date,
        DayOfWeek dayOfWeek,
        List<TimeSlot> slots
) {

    public record TimeSlot(
            LocalTime startTime,
            LocalTime endTime,
            boolean available,
            String disabledReason
    ) {
    }
}

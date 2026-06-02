package com.ringdu.server.consultation.dto;

import com.ringdu.server.consultation.entity.ConsultationAvailability;
import com.ringdu.server.consultation.entity.ConsultationAvailabilityStatus;
import com.ringdu.server.consultation.entity.ConsultationType;
import java.time.DayOfWeek;
import java.time.LocalTime;

public record ConsultationAvailabilityResponse(
        Long availabilityId,
        DayOfWeek dayOfWeek,
        String dayLabel,
        LocalTime startTime,
        LocalTime endTime,
        ConsultationType consultationType,
        ConsultationAvailabilityStatus status
) {

    public static ConsultationAvailabilityResponse of(ConsultationAvailability availability) {
        return new ConsultationAvailabilityResponse(
                availability.getId(),
                availability.getDayOfWeek(),
                dayLabel(availability.getDayOfWeek()),
                availability.getStartTime(),
                availability.getEndTime(),
                availability.getConsultationType(),
                availability.getStatus()
        );
    }

    private static String dayLabel(DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case MONDAY -> "월";
            case TUESDAY -> "화";
            case WEDNESDAY -> "수";
            case THURSDAY -> "목";
            case FRIDAY -> "금";
            case SATURDAY -> "토";
            case SUNDAY -> "일";
        };
    }
}

package com.ringdu.server.consultation.dto;

import com.ringdu.server.consultation.entity.ConsultationAvailability;
import com.ringdu.server.consultation.entity.ConsultationAvailabilityStatus;
import com.ringdu.server.consultation.entity.ConsultationConsultantType;
import com.ringdu.server.consultation.entity.ConsultationType;
import java.time.DayOfWeek;
import java.time.LocalTime;

public record ConsultationAvailabilityResponse(
        Long availabilityId,
        Long academyId,
        String academyName,
        ConsultationConsultantType consultantType,
        String consultantName,
        Long teacherUserId,
        String teacherName,
        DayOfWeek dayOfWeek,
        String dayLabel,
        LocalTime startTime,
        LocalTime endTime,
        ConsultationType consultationType,
        ConsultationAvailabilityStatus status
) {

    public static ConsultationAvailabilityResponse of(
            ConsultationAvailability availability,
            String academyName,
            String teacherName
    ) {
        return new ConsultationAvailabilityResponse(
                availability.getId(),
                availability.getAcademyId(),
                academyName,
                availability.getConsultantType(),
                availability.getConsultantType() == ConsultationConsultantType.ACADEMY_ACCOUNT ? "학원 상담" : teacherName,
                availability.getTeacherUserId(),
                teacherName,
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

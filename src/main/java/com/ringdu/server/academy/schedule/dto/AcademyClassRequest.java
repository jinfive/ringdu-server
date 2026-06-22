package com.ringdu.server.academy.schedule.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ringdu.server.academy.schedule.entity.AcademyClassDayOfWeek;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalTime;
import java.util.List;

public record AcademyClassRequest(
        @NotBlank
        @Size(max = 100)
        String name,
        AcademyClassDayOfWeek dayOfWeek,
        List<AcademyClassDayOfWeek> dayOfWeeks,
        @NotNull
        Long classroomId,
        Long teacherUserId,
        @NotNull
        @JsonFormat(pattern = "HH:mm")
        LocalTime startTime,
        @NotNull
        @JsonFormat(pattern = "HH:mm")
        LocalTime endTime,
        String memo
) {
}

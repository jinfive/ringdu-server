package com.ringdu.server.academy.schedule.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ringdu.server.academy.schedule.entity.AcademyClassDayOfWeek;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalTime;

public record AcademyClassRequest(
        @NotBlank
        @Size(max = 100)
        String name,
        @NotNull
        AcademyClassDayOfWeek dayOfWeek,
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

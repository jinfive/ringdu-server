package com.ringdu.server.academy.schedule.dto;

import jakarta.validation.constraints.NotNull;

public record AcademyClassStudentRequest(
        @NotNull
        Long studentProfileId
) {
}

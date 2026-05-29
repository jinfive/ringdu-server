package com.ringdu.server.academy.schedule.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AcademyClassroomUpdateRequest(
        @NotBlank
        @Size(max = 50)
        String name
) {
}

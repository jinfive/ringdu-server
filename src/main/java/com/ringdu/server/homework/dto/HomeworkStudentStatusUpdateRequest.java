package com.ringdu.server.homework.dto;

import com.ringdu.server.homework.entity.HomeworkStudentStatus;
import jakarta.validation.constraints.NotNull;

public record HomeworkStudentStatusUpdateRequest(
        @NotNull HomeworkStudentStatus status,
        String memo
) {
}

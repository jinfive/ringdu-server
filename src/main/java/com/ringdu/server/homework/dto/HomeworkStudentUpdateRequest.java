package com.ringdu.server.homework.dto;

import com.ringdu.server.homework.entity.HomeworkStudentStatus;
import jakarta.validation.constraints.NotNull;

public record HomeworkStudentUpdateRequest(
        @NotNull
        HomeworkStudentStatus status,
        String memo
) {
}

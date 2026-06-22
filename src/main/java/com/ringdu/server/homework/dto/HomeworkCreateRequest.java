package com.ringdu.server.homework.dto;

import com.ringdu.server.homework.entity.HomeworkTargetType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

public record HomeworkCreateRequest(
        @NotBlank String title,
        @NotBlank String content,
        @NotNull LocalDate dueDate,
        @NotNull HomeworkTargetType targetType,
        List<Long> studentProfileIds,
        String memo
) {
}

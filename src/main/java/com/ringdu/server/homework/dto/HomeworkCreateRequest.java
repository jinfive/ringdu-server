package com.ringdu.server.homework.dto;

import com.ringdu.server.homework.entity.HomeworkTargetType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

public record HomeworkCreateRequest(
        @NotBlank
        @Size(max = 150)
        String title,

        @NotBlank
        String content,

        @NotNull
        LocalDate dueDate,

        @NotNull
        HomeworkTargetType targetType,

        List<Long> studentProfileIds,

        String memo
) {
}

package com.ringdu.server.homework.dto;

import com.ringdu.server.homework.entity.Homework;
import com.ringdu.server.homework.entity.HomeworkTargetType;
import java.time.LocalDate;

public record HomeworkSummaryResponse(
        Long homeworkId,
        Long classId,
        String className,
        String title,
        String content,
        LocalDate dueDate,
        HomeworkTargetType targetType,
        long assignedCount,
        long doneCount,
        long notDoneCount,
        String memo
) {
    public static HomeworkSummaryResponse of(
            Homework homework,
            String className,
            long assignedCount,
            long doneCount,
            long notDoneCount
    ) {
        return new HomeworkSummaryResponse(
                homework.getId(),
                homework.getClassId(),
                className,
                homework.getTitle(),
                homework.getContent(),
                homework.getDueDate(),
                homework.getTargetType(),
                assignedCount,
                doneCount,
                notDoneCount,
                homework.getMemo()
        );
    }
}

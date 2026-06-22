package com.ringdu.server.homework.dto;

import com.ringdu.server.homework.entity.Homework;
import com.ringdu.server.homework.entity.HomeworkStudent;
import com.ringdu.server.homework.entity.HomeworkStudentStatus;
import com.ringdu.server.homework.entity.HomeworkTargetType;
import java.time.LocalDate;
import java.util.List;

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
    public static HomeworkSummaryResponse of(Homework homework, String className, List<HomeworkStudent> students) {
        long doneCount = students.stream().filter(item -> item.getStatus() == HomeworkStudentStatus.DONE).count();
        return new HomeworkSummaryResponse(
                homework.getId(),
                homework.getAcademyClassId(),
                className,
                homework.getTitle(),
                homework.getContent(),
                homework.getDueDate(),
                homework.getTargetType(),
                students.size(),
                doneCount,
                students.size() - doneCount,
                homework.getMemo()
        );
    }
}

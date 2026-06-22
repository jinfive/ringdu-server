package com.ringdu.server.homework.dto;

import com.ringdu.server.homework.entity.Homework;
import com.ringdu.server.homework.entity.HomeworkTargetType;
import java.time.LocalDate;
import java.util.List;

public record HomeworkDetailResponse(
        Long homeworkId,
        Long classId,
        String className,
        String title,
        String content,
        LocalDate dueDate,
        HomeworkTargetType targetType,
        String memo,
        List<HomeworkStudentItemResponse> students
) {
    public static HomeworkDetailResponse of(Homework homework, String className, List<HomeworkStudentItemResponse> students) {
        return new HomeworkDetailResponse(
                homework.getId(),
                homework.getAcademyClassId(),
                className,
                homework.getTitle(),
                homework.getContent(),
                homework.getDueDate(),
                homework.getTargetType(),
                homework.getMemo(),
                students
        );
    }
}

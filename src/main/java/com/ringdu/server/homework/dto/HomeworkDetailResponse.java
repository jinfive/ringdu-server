package com.ringdu.server.homework.dto;

import com.ringdu.server.homework.entity.Homework;
import com.ringdu.server.homework.entity.HomeworkStudent;
import com.ringdu.server.homework.entity.HomeworkStudentStatus;
import com.ringdu.server.homework.entity.HomeworkTargetType;
import com.ringdu.server.student.entity.StudentProfile;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record HomeworkDetailResponse(
        Long homeworkId,
        Long classId,
        String className,
        String title,
        String content,
        LocalDate dueDate,
        HomeworkTargetType targetType,
        String memo,
        List<StudentItem> students
) {
    public static HomeworkDetailResponse of(
            Homework homework,
            String className,
            List<HomeworkStudent> homeworkStudents,
            Map<Long, StudentProfile> studentById
    ) {
        return new HomeworkDetailResponse(
                homework.getId(),
                homework.getClassId(),
                className,
                homework.getTitle(),
                homework.getContent(),
                homework.getDueDate(),
                homework.getTargetType(),
                homework.getMemo(),
                homeworkStudents.stream()
                        .map(item -> StudentItem.of(item, studentById.get(item.getStudentProfileId())))
                        .toList()
        );
    }

    public record StudentItem(
            Long homeworkStudentId,
            Long studentProfileId,
            String studentName,
            HomeworkStudentStatus status,
            String statusLabel,
            String memo
    ) {
        public static StudentItem of(HomeworkStudent item, StudentProfile studentProfile) {
            return new StudentItem(
                    item.getId(),
                    item.getStudentProfileId(),
                    studentProfile.getName(),
                    item.getStatus(),
                    item.getStatus().getLabel(),
                    item.getMemo()
            );
        }
    }
}

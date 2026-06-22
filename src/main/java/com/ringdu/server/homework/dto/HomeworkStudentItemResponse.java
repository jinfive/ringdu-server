package com.ringdu.server.homework.dto;

import com.ringdu.server.homework.entity.HomeworkStudentStatus;

public record HomeworkStudentItemResponse(
        Long homeworkStudentId,
        Long studentProfileId,
        String studentName,
        HomeworkStudentStatus status,
        String statusLabel,
        String memo
) {
}

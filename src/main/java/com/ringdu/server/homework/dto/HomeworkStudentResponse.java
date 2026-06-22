package com.ringdu.server.homework.dto;

import com.ringdu.server.homework.entity.HomeworkStudent;
import com.ringdu.server.homework.entity.HomeworkStudentStatus;
import com.ringdu.server.student.entity.StudentProfile;

public record HomeworkStudentResponse(
        Long studentProfileId,
        String studentName
) {
    public static HomeworkStudentResponse from(StudentProfile student) {
        return new HomeworkStudentResponse(student.getId(), student.getName());
    }

    public static HomeworkStudentItemResponse item(HomeworkStudent homeworkStudent, StudentProfile student) {
        return new HomeworkStudentItemResponse(
                homeworkStudent.getId(),
                student.getId(),
                student.getName(),
                homeworkStudent.getStatus(),
                label(homeworkStudent.getStatus()),
                homeworkStudent.getMemo()
        );
    }

    private static String label(HomeworkStudentStatus status) {
        return status == HomeworkStudentStatus.DONE ? "해옴" : "안해옴";
    }
}

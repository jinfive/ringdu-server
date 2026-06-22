package com.ringdu.server.homework.dto;

import com.ringdu.server.academy.entity.Academy;
import com.ringdu.server.academy.schedule.entity.AcademyClass;
import com.ringdu.server.homework.entity.Homework;
import com.ringdu.server.homework.entity.HomeworkStudent;
import com.ringdu.server.homework.entity.HomeworkStudentStatus;
import com.ringdu.server.student.entity.StudentProfile;
import java.time.LocalDate;

public record HomeworkInquiryResponse(
        Long homeworkId,
        Long homeworkStudentId,
        Long studentProfileId,
        String studentName,
        Long academyId,
        String academyName,
        Long classId,
        String className,
        String title,
        String content,
        LocalDate dueDate,
        HomeworkStudentStatus status,
        String statusLabel,
        String memo
) {
    public static HomeworkInquiryResponse of(
            Homework homework,
            HomeworkStudent homeworkStudent,
            StudentProfile student,
            Academy academy,
            AcademyClass academyClass
    ) {
        return new HomeworkInquiryResponse(
                homework.getId(),
                homeworkStudent.getId(),
                student.getId(),
                student.getName(),
                academy.getId(),
                academy.getName(),
                academyClass.getId(),
                academyClass.getName(),
                homework.getTitle(),
                homework.getContent(),
                homework.getDueDate(),
                homeworkStudent.getStatus(),
                homeworkStudent.getStatus() == HomeworkStudentStatus.DONE ? "해옴" : "안해옴",
                homeworkStudent.getMemo()
        );
    }
}

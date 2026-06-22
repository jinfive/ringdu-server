package com.ringdu.server.homework.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ringdu.server.academy.entity.Academy;
import com.ringdu.server.academy.schedule.entity.AcademyClass;
import com.ringdu.server.academy.schedule.entity.AcademyClassDayOfWeek;
import com.ringdu.server.academy.schedule.entity.AcademyClassroom;
import java.time.LocalTime;
import java.util.List;

public record TeacherHomeworkClassResponse(
        Long classId,
        Long academyId,
        String academyName,
        String className,
        AcademyClassDayOfWeek dayOfWeek,
        String dayLabel,
        List<AcademyClassDayOfWeek> dayOfWeeks,
        List<String> dayLabels,
        @JsonFormat(pattern = "HH:mm") LocalTime startTime,
        @JsonFormat(pattern = "HH:mm") LocalTime endTime,
        String classroomName,
        long studentCount,
        List<HomeworkStudentResponse> students
) {
    public static TeacherHomeworkClassResponse of(
            AcademyClass academyClass,
            Academy academy,
            AcademyClassroom classroom,
            List<HomeworkStudentResponse> students
    ) {
        return new TeacherHomeworkClassResponse(
                academyClass.getId(),
                academy.getId(),
                academy.getName(),
                academyClass.getName(),
                academyClass.getDayOfWeek(),
                academyClass.getDayOfWeek().getLabel(),
                academyClass.getDayOfWeeks(),
                academyClass.getDayOfWeeks().stream().map(AcademyClassDayOfWeek::getLabel).toList(),
                academyClass.getStartTime(),
                academyClass.getEndTime(),
                classroom.getName(),
                students.size(),
                students
        );
    }
}

package com.ringdu.server.academy.schedule.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ringdu.server.academy.schedule.entity.AcademyClass;
import com.ringdu.server.academy.schedule.entity.AcademyClassDayOfWeek;
import com.ringdu.server.academy.schedule.entity.AcademyClassroom;
import com.ringdu.server.academy.schedule.entity.ScheduleStatus;
import com.ringdu.server.user.entity.User;

import java.time.LocalTime;
import java.util.List;

public record AcademyClassResponse(
        Long classId,
        String name,
        AcademyClassDayOfWeek dayOfWeek,
        String dayLabel,
        List<AcademyClassDayOfWeek> dayOfWeeks,
        List<String> dayLabels,
        Long classroomId,
        String classroomName,
        Long teacherUserId,
        String teacherName,
        @JsonFormat(pattern = "HH:mm")
        LocalTime startTime,
        @JsonFormat(pattern = "HH:mm")
        LocalTime endTime,
        String memo,
        long studentCount,
        ScheduleStatus status
) {

    public static AcademyClassResponse of(
            AcademyClass academyClass,
            AcademyClassroom classroom,
            User teacher,
            long studentCount
    ) {
        return new AcademyClassResponse(
                academyClass.getId(),
                academyClass.getName(),
                academyClass.getDayOfWeek(),
                academyClass.getDayOfWeek().getLabel(),
                academyClass.getDayOfWeeks(),
                academyClass.getDayOfWeeks().stream().map(AcademyClassDayOfWeek::getLabel).toList(),
                classroom.getId(),
                classroom.getName(),
                academyClass.getTeacherUserId(),
                teacher == null ? null : teacher.getName(),
                academyClass.getStartTime(),
                academyClass.getEndTime(),
                academyClass.getMemo(),
                studentCount,
                academyClass.getStatus()
        );
    }
}

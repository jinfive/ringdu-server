package com.ringdu.server.attendance.dto;

import com.ringdu.server.academy.schedule.entity.AcademyClass;
import com.ringdu.server.attendance.entity.AttendanceSession;
import com.ringdu.server.attendance.entity.AttendanceSessionStatus;
import java.time.LocalTime;

public record TeacherTodayClassResponse(
        Long classId,
        String className,
        String dayOfWeek,
        String dayLabel,
        LocalTime startTime,
        LocalTime endTime,
        String classroomName,
        long studentCount,
        Long attendanceSessionId,
        AttendanceSessionStatus attendanceStatus
) {

    public static TeacherTodayClassResponse of(
            AcademyClass academyClass,
            String classroomName,
            long studentCount,
            AttendanceSession session
    ) {
        return new TeacherTodayClassResponse(
                academyClass.getId(),
                academyClass.getName(),
                academyClass.getDayOfWeek().name(),
                academyClass.getDayOfWeek().getLabel(),
                academyClass.getStartTime(),
                academyClass.getEndTime(),
                classroomName,
                studentCount,
                session == null ? null : session.getId(),
                session == null ? null : session.getStatus()
        );
    }
}

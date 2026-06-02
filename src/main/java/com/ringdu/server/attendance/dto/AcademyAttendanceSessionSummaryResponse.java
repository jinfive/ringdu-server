package com.ringdu.server.attendance.dto;

import com.ringdu.server.academy.schedule.entity.AcademyClass;
import com.ringdu.server.attendance.entity.AttendanceSession;
import com.ringdu.server.attendance.entity.AttendanceSessionStatus;
import java.time.LocalDate;

public record AcademyAttendanceSessionSummaryResponse(
        Long attendanceSessionId,
        Long classId,
        String className,
        LocalDate attendanceDate,
        AttendanceSessionStatus status,
        long presentCount,
        long lateCount,
        long absentCount,
        long totalCount
) {

    public static AcademyAttendanceSessionSummaryResponse of(
            AttendanceSession session,
            AcademyClass academyClass,
            long presentCount,
            long lateCount,
            long absentCount
    ) {
        return new AcademyAttendanceSessionSummaryResponse(
                session.getId(),
                academyClass.getId(),
                academyClass.getName(),
                session.getAttendanceDate(),
                session.getStatus(),
                presentCount,
                lateCount,
                absentCount,
                presentCount + lateCount + absentCount
        );
    }
}

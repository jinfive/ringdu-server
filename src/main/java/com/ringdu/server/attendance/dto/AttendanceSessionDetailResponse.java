package com.ringdu.server.attendance.dto;

import com.ringdu.server.academy.schedule.entity.AcademyClass;
import com.ringdu.server.attendance.entity.AttendanceSession;
import com.ringdu.server.attendance.entity.AttendanceSessionStatus;
import java.time.LocalDate;
import java.util.List;

public record AttendanceSessionDetailResponse(
        Long attendanceSessionId,
        Long classId,
        String className,
        LocalDate attendanceDate,
        AttendanceSessionStatus status,
        List<AttendanceRecordResponse> records
) {

    public static AttendanceSessionDetailResponse of(
            AttendanceSession session,
            AcademyClass academyClass,
            List<AttendanceRecordResponse> records
    ) {
        return new AttendanceSessionDetailResponse(
                session.getId(),
                academyClass.getId(),
                academyClass.getName(),
                session.getAttendanceDate(),
                session.getStatus(),
                records
        );
    }
}

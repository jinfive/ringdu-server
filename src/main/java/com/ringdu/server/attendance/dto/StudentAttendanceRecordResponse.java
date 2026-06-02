package com.ringdu.server.attendance.dto;

import com.ringdu.server.attendance.entity.AttendanceRecordStatus;
import java.time.LocalDate;

public record StudentAttendanceRecordResponse(
        LocalDate attendanceDate,
        Long academyId,
        String academyName,
        Long classId,
        String className,
        AttendanceRecordStatus status,
        String statusLabel,
        String memo
) {
}

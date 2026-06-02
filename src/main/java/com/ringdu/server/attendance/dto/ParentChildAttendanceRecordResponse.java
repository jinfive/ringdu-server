package com.ringdu.server.attendance.dto;

import com.ringdu.server.attendance.entity.AttendanceRecordStatus;
import java.time.LocalDate;

public record ParentChildAttendanceRecordResponse(
        LocalDate attendanceDate,
        Long studentProfileId,
        String studentName,
        Long academyId,
        String academyName,
        Long classId,
        String className,
        AttendanceRecordStatus status,
        String statusLabel,
        String memo
) {
}

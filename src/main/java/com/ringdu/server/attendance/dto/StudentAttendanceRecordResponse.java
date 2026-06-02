package com.ringdu.server.attendance.dto;

import com.ringdu.server.academy.entity.Academy;
import com.ringdu.server.academy.schedule.entity.AcademyClass;
import com.ringdu.server.attendance.entity.AttendanceRecord;
import com.ringdu.server.attendance.entity.AttendanceRecordStatus;
import com.ringdu.server.attendance.entity.AttendanceSession;
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

    public static StudentAttendanceRecordResponse of(
            AttendanceSession session,
            AttendanceRecord record,
            AcademyClass academyClass,
            Academy academy
    ) {
        return new StudentAttendanceRecordResponse(
                session.getAttendanceDate(),
                academy.getId(),
                academy.getName(),
                academyClass.getId(),
                academyClass.getName(),
                record.getStatus(),
                statusLabel(record.getStatus()),
                record.getMemo()
        );
    }

    static String statusLabel(AttendanceRecordStatus status) {
        return switch (status) {
            case PRESENT -> "출석";
            case LATE -> "지각";
            case ABSENT -> "결석";
        };
    }
}

package com.ringdu.server.attendance.dto;

import com.ringdu.server.academy.schedule.entity.AcademyClass;
import com.ringdu.server.attendance.entity.AttendanceRecord;
import com.ringdu.server.attendance.entity.AttendanceRecordStatus;
import com.ringdu.server.attendance.entity.AttendanceSession;
import java.time.LocalDate;

public record AcademyStudentAttendanceRecordResponse(
        Long attendanceSessionId,
        Long recordId,
        Long classId,
        String className,
        LocalDate attendanceDate,
        AttendanceRecordStatus status,
        String memo
) {

    public static AcademyStudentAttendanceRecordResponse of(
            AttendanceSession session,
            AttendanceRecord record,
            AcademyClass academyClass
    ) {
        return new AcademyStudentAttendanceRecordResponse(
                session.getId(),
                record.getId(),
                academyClass.getId(),
                academyClass.getName(),
                session.getAttendanceDate(),
                record.getStatus(),
                record.getMemo()
        );
    }
}

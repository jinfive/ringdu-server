package com.ringdu.server.attendance.dto;

import com.ringdu.server.academy.entity.Academy;
import com.ringdu.server.academy.schedule.entity.AcademyClass;
import com.ringdu.server.attendance.entity.AttendanceRecord;
import com.ringdu.server.attendance.entity.AttendanceRecordStatus;
import com.ringdu.server.attendance.entity.AttendanceSession;
import com.ringdu.server.student.entity.StudentProfile;
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

    public static ParentChildAttendanceRecordResponse of(
            AttendanceSession session,
            AttendanceRecord record,
            StudentProfile studentProfile,
            AcademyClass academyClass,
            Academy academy
    ) {
        return new ParentChildAttendanceRecordResponse(
                session.getAttendanceDate(),
                studentProfile.getId(),
                studentProfile.getName(),
                academy.getId(),
                academy.getName(),
                academyClass.getId(),
                academyClass.getName(),
                record.getStatus(),
                StudentAttendanceRecordResponse.statusLabel(record.getStatus()),
                record.getMemo()
        );
    }
}

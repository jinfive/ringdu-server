package com.ringdu.server.attendance.dto;

import com.ringdu.server.attendance.entity.AttendanceRecord;
import com.ringdu.server.attendance.entity.AttendanceRecordStatus;
import com.ringdu.server.student.entity.StudentProfile;

public record AttendanceRecordResponse(
        Long recordId,
        Long studentProfileId,
        String studentName,
        AttendanceRecordStatus status,
        String memo
) {

    public static AttendanceRecordResponse of(AttendanceRecord record, StudentProfile student) {
        return new AttendanceRecordResponse(
                record.getId(),
                student.getId(),
                student.getName(),
                record.getStatus(),
                record.getMemo()
        );
    }
}

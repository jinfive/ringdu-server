package com.ringdu.server.attendance.entity;

import com.ringdu.server.global.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "attendance_records",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_attendance_records_session_student", columnNames = {"attendance_session_id", "student_profile_id"})
        },
        indexes = {
                @Index(name = "idx_attendance_records_session_id", columnList = "attendance_session_id"),
                @Index(name = "idx_attendance_records_student_id", columnList = "student_profile_id"),
                @Index(name = "idx_attendance_records_status", columnList = "status")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AttendanceRecord extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "attendance_session_id", nullable = false)
    private Long attendanceSessionId;

    @Column(name = "student_profile_id", nullable = false)
    private Long studentProfileId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AttendanceRecordStatus status;

    @Column(columnDefinition = "TEXT")
    private String memo;

    private AttendanceRecord(Long attendanceSessionId, Long studentProfileId) {
        this.attendanceSessionId = attendanceSessionId;
        this.studentProfileId = studentProfileId;
        this.status = AttendanceRecordStatus.PRESENT;
        this.memo = "";
    }

    public static AttendanceRecord create(Long attendanceSessionId, Long studentProfileId) {
        return new AttendanceRecord(attendanceSessionId, studentProfileId);
    }

    public void update(AttendanceRecordStatus status, String memo) {
        this.status = status;
        this.memo = memo == null ? "" : memo;
    }
}

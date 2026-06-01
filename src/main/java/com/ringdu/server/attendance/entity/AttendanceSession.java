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
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "attendance_sessions",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_attendance_sessions_class_date", columnNames = {"academy_class_id", "attendance_date"})
        },
        indexes = {
                @Index(name = "idx_attendance_sessions_academy_id", columnList = "academy_id"),
                @Index(name = "idx_attendance_sessions_class_id", columnList = "academy_class_id"),
                @Index(name = "idx_attendance_sessions_date", columnList = "attendance_date")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AttendanceSession extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "academy_id", nullable = false)
    private Long academyId;

    @Column(name = "academy_class_id", nullable = false)
    private Long academyClassId;

    @Column(name = "attendance_date", nullable = false)
    private LocalDate attendanceDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AttendanceSessionStatus status;

    private AttendanceSession(Long academyId, Long academyClassId, LocalDate attendanceDate) {
        this.academyId = academyId;
        this.academyClassId = academyClassId;
        this.attendanceDate = attendanceDate;
        this.status = AttendanceSessionStatus.OPEN;
    }

    public static AttendanceSession create(Long academyId, Long academyClassId, LocalDate attendanceDate) {
        return new AttendanceSession(academyId, academyClassId, attendanceDate);
    }

    public void complete() {
        this.status = AttendanceSessionStatus.COMPLETED;
    }
}

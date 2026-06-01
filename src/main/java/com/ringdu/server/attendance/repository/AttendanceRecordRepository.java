package com.ringdu.server.attendance.repository;

import com.ringdu.server.attendance.entity.AttendanceRecord;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {

    List<AttendanceRecord> findAllByAttendanceSessionIdOrderByIdAsc(Long attendanceSessionId);

    List<AttendanceRecord> findAllByAttendanceSessionIdIn(Collection<Long> attendanceSessionIds);

    List<AttendanceRecord> findAllByStudentProfileIdOrderByIdDesc(Long studentProfileId);

    Optional<AttendanceRecord> findByAttendanceSessionIdAndStudentProfileId(Long attendanceSessionId, Long studentProfileId);
}

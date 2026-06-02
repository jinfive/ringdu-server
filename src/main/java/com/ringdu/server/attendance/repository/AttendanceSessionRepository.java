package com.ringdu.server.attendance.repository;

import com.ringdu.server.attendance.entity.AttendanceSession;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttendanceSessionRepository extends JpaRepository<AttendanceSession, Long> {

    Optional<AttendanceSession> findByAcademyClassIdAndAttendanceDate(Long academyClassId, LocalDate attendanceDate);

    Optional<AttendanceSession> findByIdAndAcademyId(Long id, Long academyId);

    List<AttendanceSession> findAllByAcademyClassIdOrderByAttendanceDateDescIdDesc(Long academyClassId);

    List<AttendanceSession> findAllByAcademyClassIdInAndAttendanceDate(Collection<Long> academyClassIds, LocalDate attendanceDate);
}

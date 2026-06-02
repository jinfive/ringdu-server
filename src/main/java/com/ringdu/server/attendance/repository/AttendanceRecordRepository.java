package com.ringdu.server.attendance.repository;

import com.ringdu.server.attendance.entity.AttendanceRecord;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {

    List<AttendanceRecord> findAllByAttendanceSessionIdOrderByIdAsc(Long attendanceSessionId);

    List<AttendanceRecord> findAllByAttendanceSessionIdIn(Collection<Long> attendanceSessionIds);

    List<AttendanceRecord> findAllByStudentProfileIdOrderByIdDesc(Long studentProfileId);

    @Query("""
            select record
            from AttendanceRecord record
            join AttendanceSession session on session.id = record.attendanceSessionId
            join AcademyClass academyClass on academyClass.id = session.academyClassId
            where record.studentProfileId in :studentProfileIds
              and (:academyId is null or session.academyId = :academyId)
              and session.attendanceDate between :startDate and :endDate
              and academyClass.status = com.ringdu.server.academy.schedule.entity.ScheduleStatus.ACTIVE
            order by session.attendanceDate desc, record.id desc
            """)
    List<AttendanceRecord> findReadableStudentRecords(
            @Param("studentProfileIds") Collection<Long> studentProfileIds,
            @Param("academyId") Long academyId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    Optional<AttendanceRecord> findByAttendanceSessionIdAndStudentProfileId(Long attendanceSessionId, Long studentProfileId);
}

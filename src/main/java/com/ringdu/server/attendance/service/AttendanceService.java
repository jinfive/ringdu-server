package com.ringdu.server.attendance.service;

import com.ringdu.server.academy.entity.Academy;
import com.ringdu.server.academy.repository.AcademyRepository;
import com.ringdu.server.academy.schedule.entity.AcademyClass;
import com.ringdu.server.academy.schedule.entity.AcademyClassDayOfWeek;
import com.ringdu.server.academy.schedule.entity.AcademyClassStudent;
import com.ringdu.server.academy.schedule.entity.AcademyClassroom;
import com.ringdu.server.academy.schedule.entity.ScheduleStatus;
import com.ringdu.server.academy.schedule.repository.AcademyClassRepository;
import com.ringdu.server.academy.schedule.repository.AcademyClassStudentRepository;
import com.ringdu.server.academy.schedule.repository.AcademyClassroomRepository;
import com.ringdu.server.attendance.dto.AcademyAttendanceSessionSummaryResponse;
import com.ringdu.server.attendance.dto.AcademyStudentAttendanceRecordResponse;
import com.ringdu.server.attendance.dto.AttendanceAcademyOptionResponse;
import com.ringdu.server.attendance.dto.AttendanceRecordResponse;
import com.ringdu.server.attendance.dto.AttendanceRecordSaveRequest;
import com.ringdu.server.attendance.dto.AttendanceSessionDetailResponse;
import com.ringdu.server.attendance.dto.ParentChildAttendanceRecordResponse;
import com.ringdu.server.attendance.dto.StudentAttendanceRecordResponse;
import com.ringdu.server.attendance.dto.TeacherTodayClassResponse;
import com.ringdu.server.attendance.entity.AttendanceRecord;
import com.ringdu.server.attendance.entity.AttendanceRecordStatus;
import com.ringdu.server.attendance.entity.AttendanceSession;
import com.ringdu.server.attendance.repository.AttendanceRecordRepository;
import com.ringdu.server.attendance.repository.AttendanceSessionRepository;
import com.ringdu.server.global.exception.BusinessException;
import com.ringdu.server.global.exception.ErrorCode;
import com.ringdu.server.parentstudent.entity.ParentStudentRelationStatus;
import com.ringdu.server.parentstudent.repository.ParentStudentRelationRepository;
import com.ringdu.server.student.entity.StudentProfile;
import com.ringdu.server.student.repository.StudentProfileRepository;
import com.ringdu.server.student.entity.StudentStatus;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AcademyRepository academyRepository;
    private final AcademyClassRepository classRepository;
    private final AcademyClassStudentRepository classStudentRepository;
    private final AcademyClassroomRepository classroomRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final AttendanceSessionRepository sessionRepository;
    private final AttendanceRecordRepository recordRepository;
    private final ParentStudentRelationRepository parentStudentRelationRepository;

    @Transactional(readOnly = true)
    public List<TeacherTodayClassResponse> getTeacherTodayClasses(Long teacherUserId) {
        LocalDate today = LocalDate.now();
        AcademyClassDayOfWeek dayOfWeek = toAcademyDayOfWeek(today.getDayOfWeek());
        List<AcademyClass> classes = classRepository.findAllByTeacherUserIdAndDayOfWeekAndStatusOrderByStartTimeAscIdAsc(
                teacherUserId,
                dayOfWeek,
                ScheduleStatus.ACTIVE
        );
        if (classes.isEmpty()) {
            return List.of();
        }
        Map<Long, AttendanceSession> sessionByClassId = sessionRepository
                .findAllByAcademyClassIdInAndAttendanceDate(classes.stream().map(AcademyClass::getId).toList(), today)
                .stream()
                .collect(Collectors.toMap(AttendanceSession::getAcademyClassId, Function.identity()));

        return classes.stream()
                .map(academyClass -> TeacherTodayClassResponse.of(
                        academyClass,
                        getClassroom(academyClass.getAcademyId(), academyClass.getClassroomId()).getName(),
                        classStudentRepository.countByAcademyClassIdAndStatus(academyClass.getId(), ScheduleStatus.ACTIVE),
                        sessionByClassId.get(academyClass.getId())
                ))
                .toList();
    }

    @Transactional
    public AttendanceSessionDetailResponse createOrGetTeacherAttendanceSession(
            Long teacherUserId,
            Long classId,
            LocalDate attendanceDate
    ) {
        AcademyClass academyClass = getTeacherClass(teacherUserId, classId);
        AttendanceSession session = sessionRepository.findByAcademyClassIdAndAttendanceDate(classId, attendanceDate)
                .orElseGet(() -> {
                    AttendanceSession savedSession = sessionRepository.save(
                            AttendanceSession.create(academyClass.getAcademyId(), academyClass.getId(), attendanceDate)
                    );
                    List<AttendanceRecord> records = classStudentRepository
                            .findAllByAcademyClassIdAndStatus(classId, ScheduleStatus.ACTIVE)
                            .stream()
                            .map(link -> AttendanceRecord.create(savedSession.getId(), link.getStudentProfileId()))
                            .toList();
                    recordRepository.saveAll(records);
                    return savedSession;
                });
        return getTeacherAttendanceSession(teacherUserId, session.getId());
    }

    @Transactional(readOnly = true)
    public AttendanceSessionDetailResponse getTeacherAttendanceSession(Long teacherUserId, Long sessionId) {
        AttendanceSession session = getSession(sessionId);
        AcademyClass academyClass = getTeacherClass(teacherUserId, session.getAcademyClassId());
        return toDetailResponse(session, academyClass);
    }

    @Transactional
    public AttendanceSessionDetailResponse saveTeacherAttendanceRecords(
            Long teacherUserId,
            Long sessionId,
            AttendanceRecordSaveRequest request
    ) {
        AttendanceSession session = getSession(sessionId);
        AcademyClass academyClass = getTeacherClass(teacherUserId, session.getAcademyClassId());
        Map<Long, AcademyClassStudent> activeStudentLinkByStudentId = classStudentRepository
                .findAllByAcademyClassIdAndStatus(academyClass.getId(), ScheduleStatus.ACTIVE)
                .stream()
                .collect(Collectors.toMap(AcademyClassStudent::getStudentProfileId, Function.identity()));

        for (AttendanceRecordSaveRequest.RecordItem item : request.records()) {
            if (!activeStudentLinkByStudentId.containsKey(item.studentProfileId())) {
                throw new BusinessException(ErrorCode.ATTENDANCE_RECORD_STUDENT_NOT_IN_CLASS);
            }
            AttendanceRecord record = recordRepository
                    .findByAttendanceSessionIdAndStudentProfileId(session.getId(), item.studentProfileId())
                    .orElseGet(() -> recordRepository.save(AttendanceRecord.create(session.getId(), item.studentProfileId())));
            record.update(item.status(), item.memo());
        }
        session.complete();
        return toDetailResponse(session, academyClass);
    }

    @Transactional(readOnly = true)
    public List<AcademyAttendanceSessionSummaryResponse> getAcademyClassAttendanceSessions(Long userId, Long classId) {
        Academy academy = getAcademy(userId);
        AcademyClass academyClass = getAcademyClass(academy.getId(), classId);
        return sessionRepository.findAllByAcademyClassIdOrderByAttendanceDateDescIdDesc(classId)
                .stream()
                .map(session -> toSummaryResponse(session, academyClass))
                .toList();
    }

    @Transactional(readOnly = true)
    public AttendanceSessionDetailResponse getAcademyAttendanceSession(Long userId, Long sessionId) {
        Academy academy = getAcademy(userId);
        AttendanceSession session = sessionRepository.findByIdAndAcademyId(sessionId, academy.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ATTENDANCE_SESSION_NOT_FOUND));
        AcademyClass academyClass = getAcademyClass(academy.getId(), session.getAcademyClassId());
        return toDetailResponse(session, academyClass);
    }

    @Transactional(readOnly = true)
    public List<AcademyStudentAttendanceRecordResponse> getAcademyStudentAttendanceRecords(Long userId, Long studentProfileId) {
        Academy academy = getAcademy(userId);
        getStudentProfile(academy.getId(), studentProfileId);
        return recordRepository.findAllByStudentProfileIdOrderByIdDesc(studentProfileId)
                .stream()
                .map(record -> {
                    AttendanceSession session = sessionRepository.findByIdAndAcademyId(record.getAttendanceSessionId(), academy.getId())
                            .orElseThrow(() -> new BusinessException(ErrorCode.ATTENDANCE_SESSION_NOT_FOUND));
                    AcademyClass academyClass = getAcademyClass(academy.getId(), session.getAcademyClassId());
                    return AcademyStudentAttendanceRecordResponse.of(session, record, academyClass);
                })
                .sorted(Comparator.comparing(AcademyStudentAttendanceRecordResponse::attendanceDate).reversed())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AttendanceAcademyOptionResponse> getStudentAcademies(Long studentUserId) {
        List<StudentProfile> profiles = getActiveStudentProfiles(studentUserId);
        return toAcademyOptions(profiles);
    }

    @Transactional(readOnly = true)
    public List<StudentAttendanceRecordResponse> getStudentAttendanceRecords(
            Long studentUserId,
            Long academyId,
            Integer year,
            Integer month
    ) {
        List<StudentProfile> profiles = getActiveStudentProfiles(studentUserId)
                .stream()
                .filter(profile -> academyId == null || academyId.equals(profile.getAcademyId()))
                .toList();
        DateRange range = dateRange(year, month);
        return profiles.stream()
                .flatMap(profile -> getFilteredRecords(profile, range, academyId)
                        .stream()
                        .map(this::toStudentAttendanceResponse))
                .sorted(Comparator.comparing(StudentAttendanceRecordResponse::attendanceDate).reversed())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AttendanceAcademyOptionResponse> getParentChildAcademies(Long parentUserId, Long studentProfileId) {
        StudentProfile profile = getParentChildProfile(parentUserId, studentProfileId);
        return toAcademyOptions(List.of(profile));
    }

    @Transactional(readOnly = true)
    public List<ParentChildAttendanceRecordResponse> getParentChildAttendanceRecords(
            Long parentUserId,
            Long studentProfileId,
            Long academyId,
            Integer year,
            Integer month
    ) {
        StudentProfile profile = getParentChildProfile(parentUserId, studentProfileId);
        if (academyId != null && !academyId.equals(profile.getAcademyId())) {
            return List.of();
        }
        DateRange range = dateRange(year, month);
        return getFilteredRecords(profile, range, academyId)
                .stream()
                .map(recordContext -> toParentChildAttendanceResponse(profile, recordContext))
                .sorted(Comparator.comparing(ParentChildAttendanceRecordResponse::attendanceDate).reversed())
                .toList();
    }

    private AttendanceSessionDetailResponse toDetailResponse(AttendanceSession session, AcademyClass academyClass) {
        List<AttendanceRecordResponse> records = recordRepository.findAllByAttendanceSessionIdOrderByIdAsc(session.getId())
                .stream()
                .map(record -> AttendanceRecordResponse.of(record, getStudentProfile(academyClass.getAcademyId(), record.getStudentProfileId())))
                .toList();
        return AttendanceSessionDetailResponse.of(session, academyClass, records);
    }

    private AcademyAttendanceSessionSummaryResponse toSummaryResponse(AttendanceSession session, AcademyClass academyClass) {
        Map<AttendanceRecordStatus, Long> counts = new EnumMap<>(AttendanceRecordStatus.class);
        for (AttendanceRecordStatus status : AttendanceRecordStatus.values()) {
            counts.put(status, 0L);
        }
        recordRepository.findAllByAttendanceSessionIdOrderByIdAsc(session.getId())
                .forEach(record -> counts.compute(record.getStatus(), (key, value) -> value == null ? 1L : value + 1));
        return AcademyAttendanceSessionSummaryResponse.of(
                session,
                academyClass,
                counts.get(AttendanceRecordStatus.PRESENT),
                counts.get(AttendanceRecordStatus.LATE),
                counts.get(AttendanceRecordStatus.ABSENT)
        );
    }

    private Academy getAcademy(Long userId) {
        return academyRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ACADEMY_NOT_FOUND));
    }

    private List<StudentProfile> getActiveStudentProfiles(Long studentUserId) {
        return studentProfileRepository.findAllByUserIdAndStatus(studentUserId, StudentStatus.ACTIVE);
    }

    private StudentProfile getParentChildProfile(Long parentUserId, Long studentProfileId) {
        StudentProfile profile = studentProfileRepository.findById(studentProfileId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STUDENT_NOT_FOUND));
        if (profile.getStatus() != StudentStatus.ACTIVE || profile.getUserId() == null) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        boolean connected = parentStudentRelationRepository.existsByParentIdAndStudentIdAndStatus(
                parentUserId,
                profile.getUserId(),
                ParentStudentRelationStatus.ACTIVE
        );
        if (!connected) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        return profile;
    }

    private List<AttendanceAcademyOptionResponse> toAcademyOptions(List<StudentProfile> profiles) {
        if (profiles.isEmpty()) {
            return List.of();
        }
        Map<Long, Academy> academyById = academyRepository.findAllById(
                        profiles.stream().map(StudentProfile::getAcademyId).distinct().toList()
                )
                .stream()
                .collect(Collectors.toMap(Academy::getId, Function.identity()));
        return profiles.stream()
                .map(StudentProfile::getAcademyId)
                .distinct()
                .map(academyId -> academyById.get(academyId))
                .filter(academy -> academy != null)
                .map(academy -> new AttendanceAcademyOptionResponse(academy.getId(), academy.getName()))
                .toList();
    }

    private List<RecordContext> getFilteredRecords(StudentProfile profile, DateRange range, Long academyId) {
        return recordRepository.findAllByStudentProfileIdOrderByIdDesc(profile.getId())
                .stream()
                .map(record -> toRecordContext(record, academyId))
                .filter(context -> context != null)
                .filter(context -> !context.session().getAttendanceDate().isBefore(range.start()))
                .filter(context -> !context.session().getAttendanceDate().isAfter(range.end()))
                .toList();
    }

    private RecordContext toRecordContext(AttendanceRecord record, Long academyId) {
        AttendanceSession session = sessionRepository.findById(record.getAttendanceSessionId())
                .orElse(null);
        if (session == null || (academyId != null && !academyId.equals(session.getAcademyId()))) {
            return null;
        }
        AcademyClass academyClass = classRepository.findById(session.getAcademyClassId())
                .orElse(null);
        Academy academy = academyRepository.findById(session.getAcademyId())
                .orElse(null);
        if (academyClass == null || academy == null) {
            return null;
        }
        return new RecordContext(record, session, academyClass, academy);
    }

    private StudentAttendanceRecordResponse toStudentAttendanceResponse(RecordContext context) {
        return new StudentAttendanceRecordResponse(
                context.session().getAttendanceDate(),
                context.academy().getId(),
                context.academy().getName(),
                context.academyClass().getId(),
                context.academyClass().getName(),
                context.record().getStatus(),
                statusLabel(context.record().getStatus()),
                context.record().getMemo()
        );
    }

    private ParentChildAttendanceRecordResponse toParentChildAttendanceResponse(StudentProfile profile, RecordContext context) {
        return new ParentChildAttendanceRecordResponse(
                context.session().getAttendanceDate(),
                profile.getId(),
                profile.getName(),
                context.academy().getId(),
                context.academy().getName(),
                context.academyClass().getId(),
                context.academyClass().getName(),
                context.record().getStatus(),
                statusLabel(context.record().getStatus()),
                context.record().getMemo()
        );
    }

    private DateRange dateRange(Integer year, Integer month) {
        if (month != null && (month < 1 || month > 12)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
        YearMonth yearMonth = year == null || month == null
                ? YearMonth.now()
                : YearMonth.of(year, month);
        return new DateRange(yearMonth.atDay(1), yearMonth.atEndOfMonth());
    }

    private String statusLabel(AttendanceRecordStatus status) {
        return switch (status) {
            case PRESENT -> "출석";
            case LATE -> "지각";
            case ABSENT -> "결석";
        };
    }

    private AttendanceSession getSession(Long sessionId) {
        return sessionRepository.findById(sessionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ATTENDANCE_SESSION_NOT_FOUND));
    }

    private AcademyClass getTeacherClass(Long teacherUserId, Long classId) {
        AcademyClass academyClass = classRepository.findById(classId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ACADEMY_CLASS_NOT_FOUND));
        if (academyClass.getStatus() != ScheduleStatus.ACTIVE || !teacherUserId.equals(academyClass.getTeacherUserId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        return academyClass;
    }

    private AcademyClass getAcademyClass(Long academyId, Long classId) {
        return classRepository.findByIdAndAcademyId(classId, academyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ACADEMY_CLASS_NOT_FOUND));
    }

    private AcademyClassroom getClassroom(Long academyId, Long classroomId) {
        return classroomRepository.findByIdAndAcademyId(classroomId, academyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CLASSROOM_NOT_FOUND));
    }

    private StudentProfile getStudentProfile(Long academyId, Long studentProfileId) {
        StudentProfile studentProfile = studentProfileRepository.findById(studentProfileId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STUDENT_NOT_FOUND));
        if (!academyId.equals(studentProfile.getAcademyId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        return studentProfile;
    }

    private AcademyClassDayOfWeek toAcademyDayOfWeek(DayOfWeek dayOfWeek) {
        return AcademyClassDayOfWeek.valueOf(dayOfWeek.name());
    }

    private record DateRange(LocalDate start, LocalDate end) {
    }

    private record RecordContext(
            AttendanceRecord record,
            AttendanceSession session,
            AcademyClass academyClass,
            Academy academy
    ) {
    }
}

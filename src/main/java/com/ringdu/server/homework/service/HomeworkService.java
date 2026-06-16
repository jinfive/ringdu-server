package com.ringdu.server.homework.service;

import com.ringdu.server.academy.entity.Academy;
import com.ringdu.server.academy.repository.AcademyRepository;
import com.ringdu.server.academy.schedule.entity.AcademyClass;
import com.ringdu.server.academy.schedule.entity.AcademyClassStudent;
import com.ringdu.server.academy.schedule.entity.AcademyClassroom;
import com.ringdu.server.academy.schedule.entity.ScheduleStatus;
import com.ringdu.server.academy.schedule.repository.AcademyClassRepository;
import com.ringdu.server.academy.schedule.repository.AcademyClassStudentRepository;
import com.ringdu.server.academy.schedule.repository.AcademyClassroomRepository;
import com.ringdu.server.global.exception.BusinessException;
import com.ringdu.server.global.exception.ErrorCode;
import com.ringdu.server.homework.dto.HomeworkCreateRequest;
import com.ringdu.server.homework.dto.HomeworkDetailResponse;
import com.ringdu.server.homework.dto.HomeworkInquiryResponse;
import com.ringdu.server.homework.dto.HomeworkStudentUpdateRequest;
import com.ringdu.server.homework.dto.HomeworkSummaryResponse;
import com.ringdu.server.homework.dto.TeacherHomeworkClassResponse;
import com.ringdu.server.homework.entity.Homework;
import com.ringdu.server.homework.entity.HomeworkStatus;
import com.ringdu.server.homework.entity.HomeworkStudent;
import com.ringdu.server.homework.entity.HomeworkStudentStatus;
import com.ringdu.server.homework.entity.HomeworkTargetType;
import com.ringdu.server.homework.repository.HomeworkRepository;
import com.ringdu.server.homework.repository.HomeworkStudentRepository;
import com.ringdu.server.parentstudent.entity.ParentStudentRelationStatus;
import com.ringdu.server.parentstudent.repository.ParentStudentRelationRepository;
import com.ringdu.server.student.entity.StudentProfile;
import com.ringdu.server.student.entity.StudentStatus;
import com.ringdu.server.student.repository.StudentProfileRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class HomeworkService {

    private final AcademyRepository academyRepository;
    private final AcademyClassRepository classRepository;
    private final AcademyClassStudentRepository classStudentRepository;
    private final AcademyClassroomRepository classroomRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final ParentStudentRelationRepository parentStudentRelationRepository;
    private final HomeworkRepository homeworkRepository;
    private final HomeworkStudentRepository homeworkStudentRepository;

    @Transactional(readOnly = true)
    public List<TeacherHomeworkClassResponse> getTeacherClasses(Long teacherUserId) {
        return classRepository.findAllByTeacherUserIdAndStatusOrderByIdAsc(teacherUserId, ScheduleStatus.ACTIVE)
                .stream()
                .map(academyClass -> {
                    Academy academy = getAcademy(academyClass.getAcademyId());
                    AcademyClassroom classroom = getClassroom(academyClass);
                    List<StudentProfile> students = getActiveClassStudents(academyClass).stream()
                            .sorted(Comparator.comparing(StudentProfile::getName).thenComparing(StudentProfile::getId))
                            .toList();
                    return TeacherHomeworkClassResponse.of(academyClass, academy.getName(), classroom, students);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<HomeworkSummaryResponse> getTeacherClassHomeworks(Long teacherUserId, Long classId) {
        AcademyClass academyClass = getTeacherClass(teacherUserId, classId);
        return homeworkRepository.findAllByClassIdAndStatusOrderByDueDateDescIdDesc(classId, HomeworkStatus.ACTIVE)
                .stream()
                .map(homework -> toSummary(homework, academyClass.getName()))
                .toList();
    }

    @Transactional
    public HomeworkDetailResponse createTeacherHomework(
            Long teacherUserId,
            Long classId,
            HomeworkCreateRequest request
    ) {
        AcademyClass academyClass = getTeacherClass(teacherUserId, classId);
        List<StudentProfile> targetStudents = resolveTargetStudents(academyClass, request);
        Homework homework = homeworkRepository.save(Homework.create(
                academyClass.getAcademyId(),
                academyClass.getId(),
                request.title().trim(),
                request.content().trim(),
                request.dueDate(),
                request.targetType(),
                normalizeMemo(request.memo()),
                teacherUserId
        ));
        homeworkStudentRepository.saveAll(targetStudents.stream()
                .map(student -> HomeworkStudent.create(
                        homework.getId(),
                        academyClass.getAcademyId(),
                        academyClass.getId(),
                        student.getId()
                ))
                .toList());
        return getTeacherHomeworkDetail(teacherUserId, classId, homework.getId());
    }

    @Transactional(readOnly = true)
    public HomeworkDetailResponse getTeacherHomeworkDetail(Long teacherUserId, Long classId, Long homeworkId) {
        AcademyClass academyClass = getTeacherClass(teacherUserId, classId);
        Homework homework = getActiveHomework(classId, homeworkId);
        List<HomeworkStudent> homeworkStudents = homeworkStudentRepository.findAllByHomeworkIdOrderByIdAsc(homeworkId);
        Map<Long, StudentProfile> studentById = getStudentsById(
                homeworkStudents.stream().map(HomeworkStudent::getStudentProfileId).toList()
        );
        return HomeworkDetailResponse.of(homework, academyClass.getName(), homeworkStudents, studentById);
    }

    @Transactional
    public HomeworkDetailResponse updateTeacherHomeworkStudent(
            Long teacherUserId,
            Long homeworkStudentId,
            HomeworkStudentUpdateRequest request
    ) {
        HomeworkStudent homeworkStudent = homeworkStudentRepository.findById(homeworkStudentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.HOMEWORK_STUDENT_NOT_FOUND));
        Homework homework = homeworkRepository.findByIdAndStatus(homeworkStudent.getHomeworkId(), HomeworkStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(ErrorCode.HOMEWORK_NOT_FOUND));
        getTeacherClass(teacherUserId, homework.getClassId());
        homeworkStudent.update(request.status(), normalizeMemo(request.memo()));
        return getTeacherHomeworkDetail(teacherUserId, homework.getClassId(), homework.getId());
    }

    @Transactional
    public void deleteTeacherHomework(Long teacherUserId, Long classId, Long homeworkId) {
        getTeacherClass(teacherUserId, classId);
        Homework homework = getActiveHomework(classId, homeworkId);
        homework.delete();
    }

    @Transactional(readOnly = true)
    public List<HomeworkInquiryResponse> getStudentHomeworks(
            Long studentUserId,
            HomeworkStudentStatus status,
            LocalDate from,
            LocalDate to
    ) {
        validateDateRange(from, to);
        List<StudentProfile> profiles = studentProfileRepository
                .findAllByUserIdAndStatusOrderByNameAscIdAsc(studentUserId, StudentStatus.ACTIVE);
        return getInquiryHomeworks(profiles, status, from, to);
    }

    @Transactional(readOnly = true)
    public List<HomeworkInquiryResponse> getParentChildHomeworks(
            Long parentUserId,
            Long studentProfileId,
            HomeworkStudentStatus status,
            LocalDate from,
            LocalDate to
    ) {
        validateDateRange(from, to);
        StudentProfile profile = getActiveStudent(studentProfileId);
        if (profile.getUserId() == null || !parentStudentRelationRepository.existsByParentIdAndStudentIdAndStatus(
                parentUserId,
                profile.getUserId(),
                ParentStudentRelationStatus.ACTIVE
        )) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        return getInquiryHomeworks(List.of(profile), status, from, to);
    }

    @Transactional(readOnly = true)
    public List<HomeworkInquiryResponse> getAcademyStudentHomeworks(
            Long academyUserId,
            Long studentProfileId,
            HomeworkStudentStatus status,
            LocalDate from,
            LocalDate to
    ) {
        validateDateRange(from, to);
        Academy academy = academyRepository.findByUserId(academyUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ACADEMY_NOT_FOUND));
        StudentProfile profile = getActiveStudent(studentProfileId);
        if (!academy.getId().equals(profile.getAcademyId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        return getInquiryHomeworks(List.of(profile), status, from, to);
    }

    private List<HomeworkInquiryResponse> getInquiryHomeworks(
            List<StudentProfile> profiles,
            HomeworkStudentStatus status,
            LocalDate from,
            LocalDate to
    ) {
        if (profiles.isEmpty()) {
            return List.of();
        }
        Map<Long, StudentProfile> studentById = profiles.stream()
                .collect(Collectors.toMap(StudentProfile::getId, Function.identity()));
        List<HomeworkStudent> assignments = homeworkStudentRepository
                .findAllByStudentProfileIdInOrderByIdDesc(studentById.keySet());
        Set<Long> homeworkIds = assignments.stream().map(HomeworkStudent::getHomeworkId)
                .collect(Collectors.toSet());
        Map<Long, Homework> homeworkById = homeworkRepository.findAllByIdInAndStatus(homeworkIds, HomeworkStatus.ACTIVE)
                .stream()
                .collect(Collectors.toMap(Homework::getId, Function.identity()));

        List<HomeworkInquiryResponse> responses = new ArrayList<>();
        for (HomeworkStudent assignment : assignments) {
            Homework homework = homeworkById.get(assignment.getHomeworkId());
            if (homework == null || (status != null && assignment.getStatus() != status)) {
                continue;
            }
            if ((from != null && homework.getDueDate().isBefore(from))
                    || (to != null && homework.getDueDate().isAfter(to))) {
                continue;
            }
            StudentProfile studentProfile = studentById.get(assignment.getStudentProfileId());
            Academy academy = getAcademy(homework.getAcademyId());
            AcademyClass academyClass = classRepository.findByIdAndAcademyId(homework.getClassId(), academy.getId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.ACADEMY_CLASS_NOT_FOUND));
            responses.add(HomeworkInquiryResponse.of(homework, assignment, studentProfile, academy, academyClass));
        }
        return responses.stream()
                .sorted(Comparator.comparing(HomeworkInquiryResponse::dueDate).reversed()
                        .thenComparing(HomeworkInquiryResponse::homeworkId, Comparator.reverseOrder()))
                .toList();
    }

    private HomeworkSummaryResponse toSummary(Homework homework, String className) {
        long doneCount = homeworkStudentRepository.countByHomeworkIdAndStatus(homework.getId(), HomeworkStudentStatus.DONE);
        long notDoneCount = homeworkStudentRepository.countByHomeworkIdAndStatus(homework.getId(), HomeworkStudentStatus.NOT_DONE);
        return HomeworkSummaryResponse.of(homework, className, doneCount + notDoneCount, doneCount, notDoneCount);
    }

    private List<StudentProfile> resolveTargetStudents(AcademyClass academyClass, HomeworkCreateRequest request) {
        List<StudentProfile> activeStudents = getActiveClassStudents(academyClass);
        if (request.targetType() == HomeworkTargetType.CLASS) {
            return activeStudents;
        }
        Set<Long> requestedIds = new LinkedHashSet<>(request.studentProfileIds() == null
                ? List.of()
                : request.studentProfileIds());
        if (requestedIds.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
        Map<Long, StudentProfile> activeStudentById = activeStudents.stream()
                .collect(Collectors.toMap(StudentProfile::getId, Function.identity()));
        if (!activeStudentById.keySet().containsAll(requestedIds)) {
            throw new BusinessException(ErrorCode.HOMEWORK_STUDENT_NOT_IN_CLASS);
        }
        return requestedIds.stream().map(activeStudentById::get).toList();
    }

    private List<StudentProfile> getActiveClassStudents(AcademyClass academyClass) {
        List<Long> studentIds = classStudentRepository
                .findAllByAcademyClassIdAndStatus(academyClass.getId(), ScheduleStatus.ACTIVE)
                .stream()
                .map(AcademyClassStudent::getStudentProfileId)
                .toList();
        return studentProfileRepository.findAllById(studentIds).stream()
                .filter(student -> academyClass.getAcademyId().equals(student.getAcademyId()))
                .filter(student -> student.getStatus() == StudentStatus.ACTIVE)
                .toList();
    }

    private AcademyClass getTeacherClass(Long teacherUserId, Long classId) {
        AcademyClass academyClass = classRepository.findById(classId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ACADEMY_CLASS_NOT_FOUND));
        if (academyClass.getStatus() != ScheduleStatus.ACTIVE
                || !teacherUserId.equals(academyClass.getTeacherUserId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        return academyClass;
    }

    private Homework getActiveHomework(Long classId, Long homeworkId) {
        return homeworkRepository.findByIdAndClassIdAndStatus(homeworkId, classId, HomeworkStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(ErrorCode.HOMEWORK_NOT_FOUND));
    }

    private StudentProfile getActiveStudent(Long studentProfileId) {
        StudentProfile profile = studentProfileRepository.findById(studentProfileId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STUDENT_NOT_FOUND));
        if (profile.getStatus() != StudentStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.STUDENT_NOT_FOUND);
        }
        return profile;
    }

    private Map<Long, StudentProfile> getStudentsById(Collection<Long> studentIds) {
        return studentProfileRepository.findAllById(studentIds).stream()
                .collect(Collectors.toMap(StudentProfile::getId, Function.identity()));
    }

    private Academy getAcademy(Long academyId) {
        return academyRepository.findById(academyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ACADEMY_NOT_FOUND));
    }

    private AcademyClassroom getClassroom(AcademyClass academyClass) {
        return classroomRepository.findByIdAndAcademyId(academyClass.getClassroomId(), academyClass.getAcademyId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CLASSROOM_NOT_FOUND));
    }

    private void validateDateRange(LocalDate from, LocalDate to) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private String normalizeMemo(String memo) {
        return memo == null || memo.isBlank() ? null : memo.trim();
    }
}

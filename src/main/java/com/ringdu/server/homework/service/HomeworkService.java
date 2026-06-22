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
import com.ringdu.server.homework.dto.HomeworkStudentItemResponse;
import com.ringdu.server.homework.dto.HomeworkStudentResponse;
import com.ringdu.server.homework.dto.HomeworkStudentStatusUpdateRequest;
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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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
                .map(this::toTeacherHomeworkClass)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<HomeworkSummaryResponse> getTeacherClassHomeworks(Long teacherUserId, Long classId) {
        AcademyClass academyClass = getTeacherClass(teacherUserId, classId);
        return homeworkRepository.findAllByAcademyClassIdAndStatusOrderByDueDateDescIdDesc(classId, HomeworkStatus.ACTIVE)
                .stream()
                .map(homework -> HomeworkSummaryResponse.of(
                        homework,
                        academyClass.getName(),
                        homeworkStudentRepository.findAllByHomeworkIdOrderByIdAsc(homework.getId())
                ))
                .toList();
    }

    @Transactional
    public HomeworkDetailResponse createTeacherClassHomework(Long teacherUserId, Long classId, HomeworkCreateRequest request) {
        AcademyClass academyClass = getTeacherClass(teacherUserId, classId);
        List<Long> targetStudentIds = resolveTargetStudentIds(academyClass, request);
        if (targetStudentIds.isEmpty()) {
            throw new BusinessException(ErrorCode.HOMEWORK_CLASS_HAS_NO_STUDENTS);
        }

        Homework homework = homeworkRepository.save(Homework.create(
                academyClass.getAcademyId(),
                academyClass.getId(),
                request.title().trim(),
                request.content().trim(),
                request.dueDate(),
                request.targetType(),
                normalizeMemo(request.memo())
        ));
        homeworkStudentRepository.saveAll(targetStudentIds.stream()
                .map(studentId -> HomeworkStudent.create(homework.getId(), studentId))
                .toList());
        return getTeacherClassHomeworkDetail(teacherUserId, classId, homework.getId());
    }

    @Transactional(readOnly = true)
    public HomeworkDetailResponse getTeacherClassHomeworkDetail(Long teacherUserId, Long classId, Long homeworkId) {
        AcademyClass academyClass = getTeacherClass(teacherUserId, classId);
        Homework homework = getActiveHomework(homeworkId, academyClass.getId());
        return toDetail(homework, academyClass);
    }

    @Transactional
    public HomeworkDetailResponse updateTeacherHomeworkStudent(Long teacherUserId, Long homeworkStudentId, HomeworkStudentStatusUpdateRequest request) {
        HomeworkStudent homeworkStudent = homeworkStudentRepository.findById(homeworkStudentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.HOMEWORK_STUDENT_NOT_FOUND));
        Homework homework = homeworkRepository.findById(homeworkStudent.getHomeworkId())
                .orElseThrow(() -> new BusinessException(ErrorCode.HOMEWORK_NOT_FOUND));
        AcademyClass academyClass = getTeacherClass(teacherUserId, homework.getAcademyClassId());
        homeworkStudent.update(request.status(), normalizeMemo(request.memo()));
        return toDetail(homework, academyClass);
    }

    @Transactional
    public void deleteTeacherClassHomework(Long teacherUserId, Long classId, Long homeworkId) {
        AcademyClass academyClass = getTeacherClass(teacherUserId, classId);
        Homework homework = getActiveHomework(homeworkId, academyClass.getId());
        homework.delete();
    }

    @Transactional(readOnly = true)
    public List<HomeworkInquiryResponse> getAcademyStudentHomeworks(
            Long academyUserId,
            Long studentProfileId,
            HomeworkStudentStatus status,
            LocalDate from,
            LocalDate to
    ) {
        Academy academy = getAcademyByUser(academyUserId);
        StudentProfile student = getStudentProfile(academy.getId(), studentProfileId);
        return inquiry(List.of(student), status, from, to);
    }

    @Transactional(readOnly = true)
    public List<HomeworkInquiryResponse> getStudentHomeworks(Long studentUserId, HomeworkStudentStatus status, LocalDate from, LocalDate to) {
        List<StudentProfile> profiles = studentProfileRepository.findAllByUserIdAndStatusOrderByNameAscIdAsc(studentUserId, StudentStatus.ACTIVE);
        return inquiry(profiles, status, from, to);
    }

    @Transactional(readOnly = true)
    public List<HomeworkInquiryResponse> getParentChildHomeworks(
            Long parentUserId,
            Long studentProfileId,
            HomeworkStudentStatus status,
            LocalDate from,
            LocalDate to
    ) {
        StudentProfile student = getActiveStudentProfile(studentProfileId);
        if (student.getUserId() == null || !parentStudentRelationRepository.existsByParentIdAndStudentIdAndStatus(
                parentUserId,
                student.getUserId(),
                ParentStudentRelationStatus.ACTIVE
        )) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        return inquiry(List.of(student), status, from, to);
    }

    private TeacherHomeworkClassResponse toTeacherHomeworkClass(AcademyClass academyClass) {
        Academy academy = academyRepository.findById(academyClass.getAcademyId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ACADEMY_NOT_FOUND));
        AcademyClassroom classroom = classroomRepository.findByIdAndAcademyId(academyClass.getClassroomId(), academyClass.getAcademyId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CLASSROOM_NOT_FOUND));
        List<HomeworkStudentResponse> students = classStudentRepository
                .findAllByAcademyClassIdAndStatus(academyClass.getId(), ScheduleStatus.ACTIVE)
                .stream()
                .map(link -> getStudentProfile(academyClass.getAcademyId(), link.getStudentProfileId()))
                .map(HomeworkStudentResponse::from)
                .toList();
        return TeacherHomeworkClassResponse.of(academyClass, academy, classroom, students);
    }

    private List<Long> resolveTargetStudentIds(AcademyClass academyClass, HomeworkCreateRequest request) {
        List<Long> activeStudentIds = classStudentRepository.findAllByAcademyClassIdAndStatus(academyClass.getId(), ScheduleStatus.ACTIVE)
                .stream()
                .map(AcademyClassStudent::getStudentProfileId)
                .toList();
        if (request.targetType() == HomeworkTargetType.CLASS) {
            return activeStudentIds;
        }

        List<Long> requestedIds = request.studentProfileIds() == null ? List.of() : request.studentProfileIds().stream().distinct().toList();
        for (Long studentId : requestedIds) {
            if (!activeStudentIds.contains(studentId)) {
                throw new BusinessException(ErrorCode.ATTENDANCE_RECORD_STUDENT_NOT_IN_CLASS);
            }
        }
        return requestedIds;
    }

    private HomeworkDetailResponse toDetail(Homework homework, AcademyClass academyClass) {
        List<HomeworkStudentItemResponse> students = homeworkStudentRepository.findAllByHomeworkIdOrderByIdAsc(homework.getId())
                .stream()
                .map(item -> HomeworkStudentResponse.item(item, getStudentProfile(academyClass.getAcademyId(), item.getStudentProfileId())))
                .toList();
        return HomeworkDetailResponse.of(homework, academyClass.getName(), students);
    }

    private List<HomeworkInquiryResponse> inquiry(List<StudentProfile> profiles, HomeworkStudentStatus status, LocalDate from, LocalDate to) {
        if (profiles.isEmpty()) {
            return List.of();
        }
        Map<Long, StudentProfile> studentById = profiles.stream()
                .collect(Collectors.toMap(StudentProfile::getId, Function.identity()));
        List<HomeworkStudent> homeworkStudents = homeworkStudentRepository.findReadableHomeworks(studentById.keySet(), status, from, to);
        if (homeworkStudents.isEmpty()) {
            return List.of();
        }
        Map<Long, Homework> homeworkById = homeworkRepository.findAllByIdInAndStatus(
                        homeworkStudents.stream().map(HomeworkStudent::getHomeworkId).distinct().toList(),
                        HomeworkStatus.ACTIVE
                )
                .stream()
                .collect(Collectors.toMap(Homework::getId, Function.identity()));
        Map<Long, AcademyClass> classById = classRepository.findAllById(
                        homeworkById.values().stream().map(Homework::getAcademyClassId).distinct().toList()
                )
                .stream()
                .collect(Collectors.toMap(AcademyClass::getId, Function.identity()));
        Map<Long, Academy> academyById = academyRepository.findAllById(
                        classById.values().stream().map(AcademyClass::getAcademyId).distinct().toList()
                )
                .stream()
                .collect(Collectors.toMap(Academy::getId, Function.identity()));

        return homeworkStudents.stream()
                .map(item -> toInquiry(item, studentById, homeworkById, classById, academyById))
                .toList();
    }

    private HomeworkInquiryResponse toInquiry(
            HomeworkStudent homeworkStudent,
            Map<Long, StudentProfile> studentById,
            Map<Long, Homework> homeworkById,
            Map<Long, AcademyClass> classById,
            Map<Long, Academy> academyById
    ) {
        Homework homework = homeworkById.get(homeworkStudent.getHomeworkId());
        AcademyClass academyClass = classById.get(homework.getAcademyClassId());
        return HomeworkInquiryResponse.of(
                homework,
                homeworkStudent,
                studentById.get(homeworkStudent.getStudentProfileId()),
                academyById.get(academyClass.getAcademyId()),
                academyClass
        );
    }

    private Academy getAcademyByUser(Long academyUserId) {
        return academyRepository.findByUserId(academyUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ACADEMY_NOT_FOUND));
    }

    private AcademyClass getTeacherClass(Long teacherUserId, Long classId) {
        AcademyClass academyClass = classRepository.findById(classId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ACADEMY_CLASS_NOT_FOUND));
        if (academyClass.getStatus() != ScheduleStatus.ACTIVE || !teacherUserId.equals(academyClass.getTeacherUserId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        return academyClass;
    }

    private Homework getActiveHomework(Long homeworkId, Long classId) {
        return homeworkRepository.findByIdAndAcademyClassIdAndStatus(homeworkId, classId, HomeworkStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(ErrorCode.HOMEWORK_NOT_FOUND));
    }

    private StudentProfile getStudentProfile(Long academyId, Long studentProfileId) {
        StudentProfile student = studentProfileRepository.findById(studentProfileId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STUDENT_NOT_FOUND));
        if (!academyId.equals(student.getAcademyId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        return student;
    }

    private StudentProfile getActiveStudentProfile(Long studentProfileId) {
        StudentProfile student = studentProfileRepository.findById(studentProfileId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STUDENT_NOT_FOUND));
        if (student.getStatus() != StudentStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.STUDENT_NOT_FOUND);
        }
        return student;
    }

    private String normalizeMemo(String memo) {
        return StringUtils.hasText(memo) ? memo.trim() : null;
    }
}

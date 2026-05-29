package com.ringdu.server.academy.schedule.service;

import com.ringdu.server.academy.entity.Academy;
import com.ringdu.server.academy.entity.AcademyMemberStatus;
import com.ringdu.server.academy.repository.AcademyMemberRepository;
import com.ringdu.server.academy.repository.AcademyRepository;
import com.ringdu.server.academy.schedule.dto.AcademyClassDetailResponse;
import com.ringdu.server.academy.schedule.dto.AcademyClassRequest;
import com.ringdu.server.academy.schedule.dto.AcademyClassResponse;
import com.ringdu.server.academy.schedule.dto.AcademyClassStudentResponse;
import com.ringdu.server.academy.schedule.dto.AcademyClassroomCreateRequest;
import com.ringdu.server.academy.schedule.dto.AcademyClassroomResponse;
import com.ringdu.server.academy.schedule.dto.AcademyClassroomUpdateRequest;
import com.ringdu.server.academy.schedule.entity.AcademyClass;
import com.ringdu.server.academy.schedule.entity.AcademyClassDayOfWeek;
import com.ringdu.server.academy.schedule.entity.AcademyClassStudent;
import com.ringdu.server.academy.schedule.entity.AcademyClassroom;
import com.ringdu.server.academy.schedule.entity.ScheduleStatus;
import com.ringdu.server.academy.schedule.repository.AcademyClassRepository;
import com.ringdu.server.academy.schedule.repository.AcademyClassStudentRepository;
import com.ringdu.server.academy.schedule.repository.AcademyClassroomRepository;
import com.ringdu.server.global.exception.BusinessException;
import com.ringdu.server.global.exception.ErrorCode;
import com.ringdu.server.student.entity.StudentProfile;
import com.ringdu.server.student.repository.StudentProfileRepository;
import com.ringdu.server.user.entity.User;
import com.ringdu.server.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AcademyScheduleService {

    private final AcademyRepository academyRepository;
    private final AcademyClassroomRepository classroomRepository;
    private final AcademyClassRepository classRepository;
    private final AcademyClassStudentRepository classStudentRepository;
    private final AcademyMemberRepository academyMemberRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final UserRepository userRepository;

    @Transactional
    public AcademyClassroomResponse createClassroom(Long userId, AcademyClassroomCreateRequest request) {
        Academy academy = getAcademy(userId);
        validateClassroomNameAvailable(academy.getId(), request.name(), null);
        int displayOrder = classroomRepository.countByAcademyId(academy.getId()) + 1;
        AcademyClassroom classroom = classroomRepository.save(AcademyClassroom.create(academy.getId(), request.name(), displayOrder));
        return AcademyClassroomResponse.from(classroom);
    }

    @Transactional(readOnly = true)
    public List<AcademyClassroomResponse> getClassrooms(Long userId) {
        Academy academy = getAcademy(userId);
        return classroomRepository.findAllByAcademyIdAndStatusOrderByDisplayOrderAscIdAsc(academy.getId(), ScheduleStatus.ACTIVE)
                .stream()
                .map(AcademyClassroomResponse::from)
                .toList();
    }

    @Transactional
    public AcademyClassroomResponse updateClassroom(Long userId, Long classroomId, AcademyClassroomUpdateRequest request) {
        Academy academy = getAcademy(userId);
        AcademyClassroom classroom = getClassroom(academy.getId(), classroomId);
        validateClassroomNameAvailable(academy.getId(), request.name(), classroomId);
        classroom.updateName(request.name());
        return AcademyClassroomResponse.from(classroom);
    }

    @Transactional
    public void deleteClassroom(Long userId, Long classroomId) {
        Academy academy = getAcademy(userId);
        AcademyClassroom classroom = getClassroom(academy.getId(), classroomId);
        classroom.deactivate();
    }

    @Transactional
    public AcademyClassResponse createClass(Long userId, AcademyClassRequest request) {
        Academy academy = getAcademy(userId);
        validateClassTime(request);
        AcademyClassroom classroom = getActiveClassroom(academy.getId(), request.classroomId());
        User teacher = getValidTeacher(academy.getId(), request.teacherUserId());
        validateNoOverlap(academy.getId(), request, null);

        AcademyClass academyClass = classRepository.save(AcademyClass.create(
                academy.getId(),
                classroom.getId(),
                request.teacherUserId(),
                request.name(),
                request.dayOfWeek(),
                request.startTime(),
                request.endTime(),
                request.memo()
        ));
        return AcademyClassResponse.of(academyClass, classroom, teacher, 0);
    }

    @Transactional(readOnly = true)
    public List<AcademyClassResponse> getClasses(
            Long userId,
            AcademyClassDayOfWeek dayOfWeek,
            Long classroomId,
            ScheduleStatus status
    ) {
        Academy academy = getAcademy(userId);
        if (classroomId != null) {
            getClassroom(academy.getId(), classroomId);
        }
        ScheduleStatus queryStatus = status == null ? ScheduleStatus.ACTIVE : status;
        return classRepository.findSchedule(academy.getId(), dayOfWeek, classroomId, queryStatus)
                .stream()
                .map(this::toClassResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AcademyClassDetailResponse getClass(Long userId, Long classId) {
        Academy academy = getAcademy(userId);
        AcademyClass academyClass = getAcademyClass(academy.getId(), classId);
        AcademyClassroom classroom = getClassroom(academy.getId(), academyClass.getClassroomId());
        User teacher = getTeacher(academyClass.getTeacherUserId());
        List<AcademyClassStudentResponse> students = classStudentRepository.findAllByAcademyClassIdAndStatus(classId, ScheduleStatus.ACTIVE)
                .stream()
                .map(link -> getStudentProfile(academy.getId(), link.getStudentProfileId()))
                .map(AcademyClassStudentResponse::from)
                .toList();
        return AcademyClassDetailResponse.of(academyClass, classroom, teacher, students);
    }

    @Transactional
    public AcademyClassResponse updateClass(Long userId, Long classId, AcademyClassRequest request) {
        Academy academy = getAcademy(userId);
        AcademyClass academyClass = getAcademyClass(academy.getId(), classId);
        validateClassTime(request);
        AcademyClassroom classroom = getActiveClassroom(academy.getId(), request.classroomId());
        User teacher = getValidTeacher(academy.getId(), request.teacherUserId());
        validateNoOverlap(academy.getId(), request, classId);

        academyClass.update(
                classroom.getId(),
                request.teacherUserId(),
                request.name(),
                request.dayOfWeek(),
                request.startTime(),
                request.endTime(),
                request.memo()
        );
        long studentCount = classStudentRepository.countByAcademyClassIdAndStatus(classId, ScheduleStatus.ACTIVE);
        return AcademyClassResponse.of(academyClass, classroom, teacher, studentCount);
    }

    @Transactional
    public void deleteClass(Long userId, Long classId) {
        Academy academy = getAcademy(userId);
        AcademyClass academyClass = getAcademyClass(academy.getId(), classId);
        academyClass.deactivate();
    }

    @Transactional
    public AcademyClassDetailResponse addStudent(Long userId, Long classId, Long studentProfileId) {
        Academy academy = getAcademy(userId);
        AcademyClass academyClass = getAcademyClass(academy.getId(), classId);
        getStudentProfile(academy.getId(), studentProfileId);

        classStudentRepository.findByAcademyClassIdAndStudentProfileId(classId, studentProfileId)
                .ifPresentOrElse(link -> {
                    if (link.getStatus() == ScheduleStatus.ACTIVE) {
                        throw new BusinessException(ErrorCode.ACADEMY_CLASS_STUDENT_ALREADY_EXISTS);
                    }
                    link.activate();
                }, () -> classStudentRepository.save(AcademyClassStudent.create(classId, studentProfileId)));
        return getClass(userId, academyClass.getId());
    }

    @Transactional
    public void deleteStudent(Long userId, Long classId, Long studentProfileId) {
        Academy academy = getAcademy(userId);
        getAcademyClass(academy.getId(), classId);
        getStudentProfile(academy.getId(), studentProfileId);
        AcademyClassStudent link = classStudentRepository.findByAcademyClassIdAndStudentProfileId(classId, studentProfileId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        link.deactivate();
    }

    private AcademyClassResponse toClassResponse(AcademyClass academyClass) {
        AcademyClassroom classroom = getClassroom(academyClass.getAcademyId(), academyClass.getClassroomId());
        User teacher = getTeacher(academyClass.getTeacherUserId());
        long studentCount = classStudentRepository.countByAcademyClassIdAndStatus(academyClass.getId(), ScheduleStatus.ACTIVE);
        return AcademyClassResponse.of(academyClass, classroom, teacher, studentCount);
    }

    private Academy getAcademy(Long userId) {
        return academyRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ACADEMY_NOT_FOUND));
    }

    private AcademyClassroom getClassroom(Long academyId, Long classroomId) {
        return classroomRepository.findByIdAndAcademyId(classroomId, academyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CLASSROOM_NOT_FOUND));
    }

    private AcademyClassroom getActiveClassroom(Long academyId, Long classroomId) {
        AcademyClassroom classroom = getClassroom(academyId, classroomId);
        if (classroom.getStatus() != ScheduleStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.CLASSROOM_NOT_FOUND);
        }
        return classroom;
    }

    private AcademyClass getAcademyClass(Long academyId, Long classId) {
        return classRepository.findByIdAndAcademyId(classId, academyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ACADEMY_CLASS_NOT_FOUND));
    }

    private StudentProfile getStudentProfile(Long academyId, Long studentProfileId) {
        StudentProfile studentProfile = studentProfileRepository.findById(studentProfileId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STUDENT_NOT_FOUND));
        if (!academyId.equals(studentProfile.getAcademyId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        return studentProfile;
    }

    private User getValidTeacher(Long academyId, Long teacherUserId) {
        if (teacherUserId == null) {
            return null;
        }
        boolean connected = academyMemberRepository.existsByAcademyIdAndUserIdAndStatus(
                academyId,
                teacherUserId,
                AcademyMemberStatus.ACTIVE
        );
        if (!connected) {
            throw new BusinessException(ErrorCode.ACADEMY_CLASS_TEACHER_NOT_CONNECTED);
        }
        return getTeacher(teacherUserId);
    }

    private User getTeacher(Long teacherUserId) {
        if (teacherUserId == null) {
            return null;
        }
        return userRepository.findById(teacherUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private void validateClassroomNameAvailable(Long academyId, String name, Long currentClassroomId) {
        boolean duplicated = classroomRepository.existsByAcademyIdAndNameAndStatus(academyId, name, ScheduleStatus.ACTIVE);
        if (duplicated && currentClassroomId == null) {
            throw new BusinessException(ErrorCode.CLASSROOM_ALREADY_EXISTS);
        }
        if (duplicated && currentClassroomId != null) {
            AcademyClassroom current = getClassroom(academyId, currentClassroomId);
            if (!current.getName().equals(name)) {
                throw new BusinessException(ErrorCode.CLASSROOM_ALREADY_EXISTS);
            }
        }
    }

    private void validateClassTime(AcademyClassRequest request) {
        if (!request.startTime().isBefore(request.endTime())) {
            throw new BusinessException(ErrorCode.ACADEMY_CLASS_TIME_INVALID);
        }
    }

    private void validateNoOverlap(Long academyId, AcademyClassRequest request, Long excludedClassId) {
        boolean overlap = classRepository.existsOverlappingActiveClass(
                academyId,
                request.classroomId(),
                request.dayOfWeek(),
                request.startTime(),
                request.endTime(),
                excludedClassId
        );
        if (overlap) {
            throw new BusinessException(ErrorCode.ACADEMY_CLASS_TIME_OVERLAP);
        }
    }
}

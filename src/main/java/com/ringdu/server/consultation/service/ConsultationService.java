package com.ringdu.server.consultation.service;

import com.ringdu.server.academy.entity.Academy;
import com.ringdu.server.academy.entity.AcademyMemberStatus;
import com.ringdu.server.academy.repository.AcademyMemberRepository;
import com.ringdu.server.academy.repository.AcademyRepository;
import com.ringdu.server.academy.schedule.entity.AcademyClass;
import com.ringdu.server.academy.schedule.entity.AcademyClassStudent;
import com.ringdu.server.academy.schedule.entity.ScheduleStatus;
import com.ringdu.server.academy.schedule.repository.AcademyClassRepository;
import com.ringdu.server.academy.schedule.repository.AcademyClassStudentRepository;
import com.ringdu.server.consultation.dto.ConsultationAvailabilityRequest;
import com.ringdu.server.consultation.dto.ConsultationAvailabilityResponse;
import com.ringdu.server.consultation.dto.ConsultationMemoCreateRequest;
import com.ringdu.server.consultation.dto.ConsultationMemoResponse;
import com.ringdu.server.consultation.dto.ConsultationMemoUpdateRequest;
import com.ringdu.server.consultation.dto.ConsultationRequestActionRequest;
import com.ringdu.server.consultation.dto.ConsultationRequestCreateRequest;
import com.ringdu.server.consultation.dto.ConsultationRequestResponse;
import com.ringdu.server.consultation.dto.ParentConsultationOptionResponse;
import com.ringdu.server.consultation.dto.TeacherConsultationStudentResponse;
import com.ringdu.server.consultation.entity.ConsultationAvailability;
import com.ringdu.server.consultation.entity.ConsultationAvailabilityStatus;
import com.ringdu.server.consultation.entity.ConsultationMemo;
import com.ringdu.server.consultation.entity.ConsultationMemoStatus;
import com.ringdu.server.consultation.entity.ConsultationMemoWriterRole;
import com.ringdu.server.consultation.entity.ConsultationRequest;
import com.ringdu.server.consultation.entity.ConsultationRequestStatus;
import com.ringdu.server.consultation.entity.ConsultationRequestType;
import com.ringdu.server.consultation.entity.ConsultationType;
import com.ringdu.server.consultation.repository.ConsultationAvailabilityRepository;
import com.ringdu.server.consultation.repository.ConsultationMemoRepository;
import com.ringdu.server.consultation.repository.ConsultationRequestRepository;
import com.ringdu.server.global.exception.BusinessException;
import com.ringdu.server.global.exception.ErrorCode;
import com.ringdu.server.parentstudent.entity.ParentStudentRelation;
import com.ringdu.server.parentstudent.entity.ParentStudentRelationStatus;
import com.ringdu.server.parentstudent.repository.ParentStudentRelationRepository;
import com.ringdu.server.student.entity.StudentProfile;
import com.ringdu.server.student.entity.StudentStatus;
import com.ringdu.server.student.repository.StudentProfileRepository;
import com.ringdu.server.user.entity.User;
import com.ringdu.server.user.repository.UserRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ConsultationService {

    private final AcademyRepository academyRepository;
    private final AcademyMemberRepository academyMemberRepository;
    private final AcademyClassRepository classRepository;
    private final AcademyClassStudentRepository classStudentRepository;
    private final ParentStudentRelationRepository parentStudentRelationRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final UserRepository userRepository;
    private final ConsultationAvailabilityRepository availabilityRepository;
    private final ConsultationRequestRepository requestRepository;
    private final ConsultationMemoRepository memoRepository;

    @Transactional(readOnly = true)
    public List<ConsultationAvailabilityResponse> getAcademyAvailability(Long academyUserId) {
        Academy academy = getAcademy(academyUserId);
        return availabilityRepository.findAllByAcademyIdOrderByDayOfWeekAscStartTimeAscIdAsc(academy.getId())
                .stream()
                .map(ConsultationAvailabilityResponse::of)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ConsultationAvailabilityResponse> getPublicAvailability(Long academyId, ConsultationType type) {
        ConsultationType consultationType = type == null ? ConsultationType.ENROLLED_STUDENT : type;
        return availabilityRepository.findActiveByAcademyIdAndConsultationType(
                        academyId,
                        consultationType,
                        ConsultationAvailabilityStatus.ACTIVE
                )
                .stream()
                .map(ConsultationAvailabilityResponse::of)
                .toList();
    }

    @Transactional
    public ConsultationAvailabilityResponse createAcademyAvailability(
            Long academyUserId,
            ConsultationAvailabilityRequest request
    ) {
        Academy academy = getAcademy(academyUserId);
        validateAvailabilityTime(request);
        validateAvailabilityOverlap(academy.getId(), request, null);
        ConsultationAvailability availability = availabilityRepository.save(ConsultationAvailability.create(
                academy.getId(),
                request.dayOfWeek(),
                request.startTime(),
                request.endTime(),
                request.consultationType()
        ));
        return ConsultationAvailabilityResponse.of(availability);
    }

    @Transactional
    public ConsultationAvailabilityResponse updateAcademyAvailability(
            Long academyUserId,
            Long availabilityId,
            ConsultationAvailabilityRequest request
    ) {
        Academy academy = getAcademy(academyUserId);
        ConsultationAvailability availability = availabilityRepository.findByIdAndAcademyId(availabilityId, academy.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CONSULTATION_AVAILABILITY_NOT_FOUND));
        validateAvailabilityTime(request);
        validateAvailabilityOverlap(academy.getId(), request, availability.getId());
        availability.update(request.dayOfWeek(), request.startTime(), request.endTime(), request.consultationType());
        return ConsultationAvailabilityResponse.of(availability);
    }

    @Transactional
    public ConsultationAvailabilityResponse deleteAcademyAvailability(Long academyUserId, Long availabilityId) {
        Academy academy = getAcademy(academyUserId);
        ConsultationAvailability availability = availabilityRepository.findByIdAndAcademyId(availabilityId, academy.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CONSULTATION_AVAILABILITY_NOT_FOUND));
        availability.deactivate();
        return ConsultationAvailabilityResponse.of(availability);
    }

    @Transactional(readOnly = true)
    public List<ParentConsultationOptionResponse> getParentConsultationOptions(Long parentUserId) {
        List<ParentStudentRelation> relations = parentStudentRelationRepository.findAllByParentIdAndStatusOrderByCreatedAtDesc(
                parentUserId,
                ParentStudentRelationStatus.ACTIVE
        );
        if (relations.isEmpty()) {
            return List.of();
        }
        List<Long> studentUserIds = relations.stream()
                .map(relation -> relation.getStudent().getId())
                .toList();
        List<StudentProfile> profiles = studentProfileRepository.findAllByUserIdInAndStatus(studentUserIds, StudentStatus.ACTIVE);
        Map<Long, Academy> academyById = academyRepository.findAllById(
                        profiles.stream().map(StudentProfile::getAcademyId).distinct().toList()
                )
                .stream()
                .collect(Collectors.toMap(Academy::getId, Function.identity()));

        return profiles.stream()
                .map(profile -> new ParentConsultationOptionResponse(
                        profile.getId(),
                        profile.getName(),
                        profile.getAcademyId(),
                        academyById.get(profile.getAcademyId()).getName(),
                        getTeacherOptions(profile)
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ConsultationRequestResponse> getParentRequests(Long parentUserId) {
        return requestRepository.findAllByParentUserIdOrderByCreatedAtDescIdDesc(parentUserId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ConsultationRequestResponse createParentRequest(Long parentUserId, ConsultationRequestCreateRequest request) {
        StudentProfile studentProfile = getActiveStudentProfile(request.studentProfileId());
        validateParentChild(parentUserId, studentProfile);
        if (!request.academyId().equals(studentProfile.getAcademyId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        validateRequestTime(request);
        validateTeacher(request.academyId(), request.teacherUserId());
        validateRequestAvailability(request);
        validateDuplicateRequest(request);

        ConsultationRequest savedRequest = requestRepository.save(ConsultationRequest.create(
                request.academyId(),
                request.studentProfileId(),
                parentUserId,
                request.teacherUserId(),
                request.requestedDate(),
                request.requestedStartTime(),
                request.requestedEndTime(),
                request.topic(),
                request.content()
        ));
        return toResponse(savedRequest);
    }

    @Transactional(readOnly = true)
    public List<ConsultationRequestResponse> getAcademyRequests(
            Long academyUserId,
            ConsultationRequestStatus status,
            LocalDate from,
            LocalDate to,
            ConsultationRequestType type,
            Long studentProfileId
    ) {
        Academy academy = getAcademy(academyUserId);
        return requestRepository.findAcademyRequests(
                        academy.getId(),
                        status,
                        from,
                        to,
                        type == null ? ConsultationRequestType.ENROLLED_STUDENT : type,
                        studentProfileId
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ConsultationRequestResponse approveAcademyRequest(
            Long academyUserId,
            Long requestId,
            ConsultationRequestActionRequest actionRequest
    ) {
        ConsultationRequest request = getAcademyConsultationRequest(academyUserId, requestId);
        request.approve(memo(actionRequest));
        return toResponse(request);
    }

    @Transactional
    public ConsultationRequestResponse rejectAcademyRequest(
            Long academyUserId,
            Long requestId,
            ConsultationRequestActionRequest actionRequest
    ) {
        ConsultationRequest request = getAcademyConsultationRequest(academyUserId, requestId);
        request.reject(memo(actionRequest));
        return toResponse(request);
    }

    @Transactional
    public ConsultationRequestResponse completeAcademyRequest(
            Long academyUserId,
            Long requestId,
            ConsultationRequestActionRequest actionRequest
    ) {
        ConsultationRequest request = getAcademyConsultationRequest(academyUserId, requestId);
        request.complete(memo(actionRequest));
        return toResponse(request);
    }

    @Transactional(readOnly = true)
    public List<ConsultationMemoResponse> getAcademyStudentMemos(Long academyUserId, Long studentProfileId) {
        Academy academy = getAcademy(academyUserId);
        StudentProfile studentProfile = getActiveStudentProfile(studentProfileId);
        validateAcademyStudent(academy.getId(), studentProfile);
        return toMemoResponses(memoRepository.findAllByAcademyIdAndStudentProfileIdAndStatusOrderByConsultationDateDescCreatedAtDescIdDesc(
                academy.getId(),
                studentProfileId,
                ConsultationMemoStatus.ACTIVE
        ));
    }

    @Transactional
    public ConsultationMemoResponse createAcademyStudentMemo(
            Long academyUserId,
            Long studentProfileId,
            ConsultationMemoCreateRequest request
    ) {
        Academy academy = getAcademy(academyUserId);
        StudentProfile studentProfile = getActiveStudentProfile(studentProfileId);
        validateAcademyStudent(academy.getId(), studentProfile);
        validateConsultationRequestLink(academy.getId(), studentProfileId, request.consultationRequestId());

        ConsultationMemo memo = memoRepository.save(ConsultationMemo.create(
                academy.getId(),
                studentProfileId,
                request.consultationRequestId(),
                academy.getUser().getId(),
                ConsultationMemoWriterRole.ACADEMY,
                request.title(),
                request.content(),
                request.nextAction(),
                request.consultationDate()
        ));
        return toMemoResponse(memo);
    }

    @Transactional
    public ConsultationMemoResponse updateAcademyMemo(
            Long academyUserId,
            Long memoId,
            ConsultationMemoUpdateRequest request
    ) {
        Academy academy = getAcademy(academyUserId);
        ConsultationMemo memo = getActiveMemo(memoId);
        if (!Objects.equals(memo.getAcademyId(), academy.getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        memo.update(request.title(), request.content(), request.nextAction(), request.consultationDate());
        return toMemoResponse(memo);
    }

    @Transactional(readOnly = true)
    public List<ConsultationMemoResponse> getTeacherMemos(Long teacherUserId, Long studentProfileId) {
        if (studentProfileId != null) {
            validateTeacherAssignedStudent(teacherUserId, studentProfileId);
            return toMemoResponses(memoRepository.findAllByStudentProfileIdInAndStatusOrderByConsultationDateDescCreatedAtDescIdDesc(
                    List.of(studentProfileId),
                    ConsultationMemoStatus.ACTIVE
            ));
        }

        List<Long> assignedStudentIds = getTeacherAssignedStudentIds(teacherUserId);
        if (assignedStudentIds.isEmpty()) {
            return List.of();
        }
        return toMemoResponses(memoRepository.findAllByStudentProfileIdInAndStatusOrderByConsultationDateDescCreatedAtDescIdDesc(
                assignedStudentIds,
                ConsultationMemoStatus.ACTIVE
        ));
    }

    @Transactional(readOnly = true)
    public List<TeacherConsultationStudentResponse> getTeacherConsultationStudents(Long teacherUserId) {
        List<Long> assignedStudentIds = getTeacherAssignedStudentIds(teacherUserId);
        if (assignedStudentIds.isEmpty()) {
            return List.of();
        }
        List<StudentProfile> profiles = studentProfileRepository.findAllById(assignedStudentIds)
                .stream()
                .filter(profile -> profile.getStatus() == StudentStatus.ACTIVE)
                .toList();
        Map<Long, Academy> academyById = academyRepository.findAllById(
                        profiles.stream().map(StudentProfile::getAcademyId).distinct().toList()
                )
                .stream()
                .collect(Collectors.toMap(Academy::getId, Function.identity()));
        return profiles.stream()
                .map(profile -> TeacherConsultationStudentResponse.of(
                        profile,
                        academyById.get(profile.getAcademyId()).getName()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ConsultationRequestResponse> getTeacherConsultationRequests(Long teacherUserId, Long studentProfileId) {
        List<Long> assignedStudentIds;
        if (studentProfileId != null) {
            validateTeacherAssignedStudent(teacherUserId, studentProfileId);
            assignedStudentIds = List.of(studentProfileId);
        } else {
            assignedStudentIds = getTeacherAssignedStudentIds(teacherUserId);
        }
        if (assignedStudentIds.isEmpty()) {
            return List.of();
        }
        return requestRepository.findAllByStudentProfileIdInOrderByRequestedDateDescRequestedStartTimeDescIdDesc(assignedStudentIds)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ConsultationMemoResponse createTeacherMemo(Long teacherUserId, ConsultationMemoCreateRequest request) {
        if (request.studentProfileId() == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
        StudentProfile studentProfile = validateTeacherAssignedStudent(teacherUserId, request.studentProfileId());
        validateConsultationRequestLink(studentProfile.getAcademyId(), studentProfile.getId(), request.consultationRequestId());

        ConsultationMemo memo = memoRepository.save(ConsultationMemo.create(
                studentProfile.getAcademyId(),
                studentProfile.getId(),
                request.consultationRequestId(),
                teacherUserId,
                ConsultationMemoWriterRole.TEACHER,
                request.title(),
                request.content(),
                request.nextAction(),
                request.consultationDate()
        ));
        return toMemoResponse(memo);
    }

    @Transactional
    public ConsultationMemoResponse updateTeacherMemo(
            Long teacherUserId,
            Long memoId,
            ConsultationMemoUpdateRequest request
    ) {
        ConsultationMemo memo = getActiveMemo(memoId);
        if (memo.getWriterRole() != ConsultationMemoWriterRole.TEACHER
                || !Objects.equals(memo.getWriterUserId(), teacherUserId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        validateTeacherAssignedStudent(teacherUserId, memo.getStudentProfileId());
        memo.update(request.title(), request.content(), request.nextAction(), request.consultationDate());
        return toMemoResponse(memo);
    }

    private void validateAvailabilityTime(ConsultationAvailabilityRequest request) {
        if (!request.startTime().isBefore(request.endTime())) {
            throw new BusinessException(ErrorCode.CONSULTATION_AVAILABILITY_TIME_INVALID);
        }
    }

    private void validateAvailabilityOverlap(
            Long academyId,
            ConsultationAvailabilityRequest request,
            Long excludedAvailabilityId
    ) {
        boolean overlapped = availabilityRepository.existsOverlappingActive(
                academyId,
                request.dayOfWeek(),
                request.startTime(),
                request.endTime(),
                excludedAvailabilityId
        );
        if (overlapped) {
            throw new BusinessException(ErrorCode.CONSULTATION_AVAILABILITY_TIME_OVERLAP);
        }
    }

    private void validateRequestTime(ConsultationRequestCreateRequest request) {
        if (!request.requestedStartTime().isBefore(request.requestedEndTime())) {
            throw new BusinessException(ErrorCode.CONSULTATION_REQUEST_TIME_INVALID);
        }
    }

    private void validateRequestAvailability(ConsultationRequestCreateRequest request) {
        boolean available = availabilityRepository.findActiveByAcademyIdAndConsultationType(
                        request.academyId(),
                        ConsultationType.ENROLLED_STUDENT,
                        ConsultationAvailabilityStatus.ACTIVE
                )
                .stream()
                .anyMatch(availability ->
                        availability.getDayOfWeek() == request.requestedDate().getDayOfWeek()
                                && !availability.getStartTime().isAfter(request.requestedStartTime())
                                && !request.requestedEndTime().isAfter(availability.getEndTime())
                );
        if (!available) {
            throw new BusinessException(ErrorCode.CONSULTATION_REQUEST_TIME_UNAVAILABLE);
        }
    }

    private void validateDuplicateRequest(ConsultationRequestCreateRequest request) {
        boolean exists = requestRepository.existsByAcademyIdAndStudentProfileIdAndRequestedDateAndRequestedStartTimeAndRequestedEndTimeAndStatus(
                request.academyId(),
                request.studentProfileId(),
                request.requestedDate(),
                request.requestedStartTime(),
                request.requestedEndTime(),
                ConsultationRequestStatus.REQUESTED
        );
        if (exists) {
            throw new BusinessException(ErrorCode.CONSULTATION_REQUEST_ALREADY_EXISTS);
        }
    }

    private void validateParentChild(Long parentUserId, StudentProfile studentProfile) {
        if (studentProfile.getUserId() == null) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        boolean connected = parentStudentRelationRepository.existsByParentIdAndStudentIdAndStatus(
                parentUserId,
                studentProfile.getUserId(),
                ParentStudentRelationStatus.ACTIVE
        );
        if (!connected) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }

    private void validateTeacher(Long academyId, Long teacherUserId) {
        if (teacherUserId == null) {
            return;
        }
        boolean connected = academyMemberRepository.existsByAcademyIdAndUserIdAndStatus(
                academyId,
                teacherUserId,
                AcademyMemberStatus.ACTIVE
        );
        if (!connected) {
            throw new BusinessException(ErrorCode.ACADEMY_CLASS_TEACHER_NOT_CONNECTED);
        }
    }

    private List<ParentConsultationOptionResponse.TeacherOption> getTeacherOptions(StudentProfile profile) {
        List<AcademyClassStudent> classLinks = classStudentRepository.findActiveLinksWithActiveClassByStudentProfileId(
                profile.getId(),
                ScheduleStatus.ACTIVE
        );
        Map<Long, AcademyClass> classById = classRepository.findAllById(
                        classLinks.stream().map(AcademyClassStudent::getAcademyClassId).toList()
                )
                .stream()
                .filter(academyClass -> Objects.equals(academyClass.getAcademyId(), profile.getAcademyId()))
                .collect(Collectors.toMap(AcademyClass::getId, Function.identity()));
        Map<Long, User> userById = userRepository.findAllById(
                        classById.values().stream()
                                .map(AcademyClass::getTeacherUserId)
                                .filter(Objects::nonNull)
                                .distinct()
                                .toList()
                )
                .stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        return classById.values().stream()
                .filter(academyClass -> academyClass.getTeacherUserId() != null)
                .map(academyClass -> new ParentConsultationOptionResponse.TeacherOption(
                        academyClass.getTeacherUserId(),
                        userById.get(academyClass.getTeacherUserId()).getName(),
                        academyClass.getId(),
                        academyClass.getName()
                ))
                .toList();
    }

    private ConsultationRequest getAcademyConsultationRequest(Long academyUserId, Long requestId) {
        Academy academy = getAcademy(academyUserId);
        return requestRepository.findByIdAndAcademyId(requestId, academy.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CONSULTATION_REQUEST_NOT_FOUND));
    }

    private void validateAcademyStudent(Long academyId, StudentProfile studentProfile) {
        if (!Objects.equals(studentProfile.getAcademyId(), academyId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }

    private StudentProfile validateTeacherAssignedStudent(Long teacherUserId, Long studentProfileId) {
        StudentProfile studentProfile = getActiveStudentProfile(studentProfileId);
        boolean assigned = classStudentRepository.existsActiveStudentForTeacher(
                teacherUserId,
                studentProfileId,
                ScheduleStatus.ACTIVE
        );
        if (!assigned) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        return studentProfile;
    }

    private List<Long> getTeacherAssignedStudentIds(Long teacherUserId) {
        List<Long> classIds = classRepository.findAllByTeacherUserIdAndStatusOrderByIdAsc(teacherUserId, ScheduleStatus.ACTIVE)
                .stream()
                .map(AcademyClass::getId)
                .toList();
        if (classIds.isEmpty()) {
            return List.of();
        }
        List<Long> studentProfileIds = classStudentRepository.findAllByAcademyClassIdInAndStatus(classIds, ScheduleStatus.ACTIVE)
                .stream()
                .map(AcademyClassStudent::getStudentProfileId)
                .distinct()
                .toList();
        if (studentProfileIds.isEmpty()) {
            return List.of();
        }
        return studentProfileRepository.findAllById(studentProfileIds)
                .stream()
                .filter(profile -> profile.getStatus() == StudentStatus.ACTIVE)
                .map(StudentProfile::getId)
                .toList();
    }

    private void validateConsultationRequestLink(Long academyId, Long studentProfileId, Long consultationRequestId) {
        if (consultationRequestId == null) {
            return;
        }
        ConsultationRequest request = requestRepository.findByIdAndAcademyId(consultationRequestId, academyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CONSULTATION_REQUEST_NOT_FOUND));
        if (!Objects.equals(request.getStudentProfileId(), studentProfileId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }

    private ConsultationMemo getActiveMemo(Long memoId) {
        return memoRepository.findByIdAndStatus(memoId, ConsultationMemoStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(ErrorCode.CONSULTATION_MEMO_NOT_FOUND));
    }

    private ConsultationRequestResponse toResponse(ConsultationRequest request) {
        Academy academy = academyRepository.findById(request.getAcademyId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ACADEMY_NOT_FOUND));
        StudentProfile studentProfile = studentProfileRepository.findById(request.getStudentProfileId())
                .orElseThrow(() -> new BusinessException(ErrorCode.STUDENT_NOT_FOUND));
        String teacherName = null;
        if (request.getTeacherUserId() != null) {
            teacherName = userRepository.findById(request.getTeacherUserId())
                    .map(User::getName)
                    .orElse(null);
        }
        String parentPhone = userRepository.findById(request.getParentUserId())
                .map(User::getPhone)
                .orElse(null);
        return ConsultationRequestResponse.of(request, academy.getName(), studentProfile.getName(), parentPhone, teacherName);
    }

    private List<ConsultationMemoResponse> toMemoResponses(List<ConsultationMemo> memos) {
        if (memos.isEmpty()) {
            return List.of();
        }
        Map<Long, Academy> academyById = academyRepository.findAllById(
                        memos.stream().map(ConsultationMemo::getAcademyId).distinct().toList()
                )
                .stream()
                .collect(Collectors.toMap(Academy::getId, Function.identity()));
        Map<Long, StudentProfile> studentById = studentProfileRepository.findAllById(
                        memos.stream().map(ConsultationMemo::getStudentProfileId).distinct().toList()
                )
                .stream()
                .collect(Collectors.toMap(StudentProfile::getId, Function.identity()));
        Map<Long, User> userById = userRepository.findAllById(
                        memos.stream().map(ConsultationMemo::getWriterUserId).distinct().toList()
                )
                .stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        return memos.stream()
                .map(memo -> ConsultationMemoResponse.of(
                        memo,
                        academyById.get(memo.getAcademyId()).getName(),
                        studentById.get(memo.getStudentProfileId()).getName(),
                        userById.get(memo.getWriterUserId()).getName()
                ))
                .toList();
    }

    private ConsultationMemoResponse toMemoResponse(ConsultationMemo memo) {
        Academy academy = academyRepository.findById(memo.getAcademyId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ACADEMY_NOT_FOUND));
        StudentProfile studentProfile = studentProfileRepository.findById(memo.getStudentProfileId())
                .orElseThrow(() -> new BusinessException(ErrorCode.STUDENT_NOT_FOUND));
        User writer = userRepository.findById(memo.getWriterUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return ConsultationMemoResponse.of(memo, academy.getName(), studentProfile.getName(), writer.getName());
    }

    private StudentProfile getActiveStudentProfile(Long studentProfileId) {
        StudentProfile studentProfile = studentProfileRepository.findById(studentProfileId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STUDENT_NOT_FOUND));
        if (studentProfile.getStatus() != StudentStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.STUDENT_NOT_FOUND);
        }
        return studentProfile;
    }

    private Academy getAcademy(Long userId) {
        return academyRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ACADEMY_NOT_FOUND));
    }

    private String memo(ConsultationRequestActionRequest request) {
        return request == null ? null : request.memo();
    }
}

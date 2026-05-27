package com.ringdu.server.student.service;

import com.ringdu.server.academy.entity.Academy;
import com.ringdu.server.academy.repository.AcademyRepository;
import com.ringdu.server.global.exception.BusinessException;
import com.ringdu.server.global.exception.ErrorCode;
import com.ringdu.server.student.dto.AcademyStudentCreateRequest;
import com.ringdu.server.student.dto.AcademyStudentResponse;
import com.ringdu.server.student.entity.StudentProfile;
import com.ringdu.server.student.entity.StudentStatus;
import com.ringdu.server.student.repository.StudentProfileRepository;
import com.ringdu.server.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AcademyStudentService {

    private final StudentProfileRepository studentProfileRepository;
    private final AcademyRepository academyRepository;
    private final StudentDetailAssembler studentDetailAssembler;
    private final StudentGuardianLinkService guardianLinkService;

    @Transactional
    public AcademyStudentResponse createStudent(Long userId, AcademyStudentCreateRequest request) {
        Academy academy = getAcademyByUserId(userId);

        StudentProfile studentProfile = StudentProfile.builder()
                .academyId(academy.getId())
                .name(request.name())
                .birthDate(request.birthDate())
                .school(request.school())
                .grade(request.grade())
                .email(request.email())
                .phone(request.phone())
                .guardianPhone(request.guardianPhone())
                .status(StudentStatus.ACTIVE)
                .memo(request.memo())
                .build();

        StudentProfile saved = studentProfileRepository.save(studentProfile);
        User guardianParent = guardianLinkService.resolveRegistrationGuardianParent(
                request.guardianPhone(),
                request.guardianParentUserId()
        );
        guardianLinkService.linkRegistrationGuardian(saved, guardianParent);

        return studentDetailAssembler.toDetailResponse(saved, guardianParent);
    }

    @Transactional(readOnly = true)
    public List<AcademyStudentResponse> getMyAcademyStudents(Long userId) {
        Academy academy = getAcademyByUserId(userId);
        return studentProfileRepository.findAllByAcademyId(academy.getId()).stream()
                .map(studentDetailAssembler::toListResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AcademyStudentResponse getStudent(Long userId, Long studentId) {
        Academy academy = getAcademyByUserId(userId);
        StudentProfile profile = studentProfileRepository.findById(studentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STUDENT_NOT_FOUND));

        validateAcademyOwnership(academy, profile);

        return studentDetailAssembler.toDetailResponse(profile);
    }

    @Transactional
    public AcademyStudentResponse updateStudent(Long userId, Long studentId, AcademyStudentCreateRequest request) {
        Academy academy = getAcademyByUserId(userId);
        StudentProfile profile = studentProfileRepository.findById(studentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STUDENT_NOT_FOUND));

        validateAcademyOwnership(academy, profile);

        profile.update(
                request.name(),
                request.birthDate(),
                request.school(),
                request.grade(),
                request.email(),
                request.phone(),
                request.guardianPhone(),
                profile.getStatus(),
                request.memo()
        );

        return studentDetailAssembler.toDetailResponse(profile);
    }

    private Academy getAcademyByUserId(Long userId) {
        return academyRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ACADEMY_NOT_FOUND));
    }

    private void validateAcademyOwnership(Academy academy, StudentProfile profile) {
        if (!profile.getAcademyId().equals(academy.getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }

}

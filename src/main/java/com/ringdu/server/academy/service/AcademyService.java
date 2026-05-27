package com.ringdu.server.academy.service;

import com.ringdu.server.academy.dto.AcademyDashboardResponse;
import com.ringdu.server.academy.dto.AcademyResponse;
import com.ringdu.server.academy.dto.AcademyUpdateRequest;
import com.ringdu.server.academy.entity.Academy;
import com.ringdu.server.academy.entity.AcademySignupApplication;
import com.ringdu.server.academy.repository.AcademyMemberRepository;
import com.ringdu.server.academy.repository.AcademyRepository;
import com.ringdu.server.student.repository.StudentProfileRepository;
import com.ringdu.server.global.exception.BusinessException;
import com.ringdu.server.global.exception.ErrorCode;
import com.ringdu.server.user.entity.Role;
import com.ringdu.server.user.entity.User;
import com.ringdu.server.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AcademyService {

    private final AcademyRepository academyRepository;
    private final UserRepository userRepository;
    private final AcademyMemberRepository academyMemberRepository;
    private final StudentProfileRepository studentProfileRepository;

    @Transactional
    public Academy createFromSignupApplication(AcademySignupApplication application) {
        User user = application.getUser();
        validateAcademyUser(user);
        validateAcademyDoesNotExist(user.getId());

        Academy academy = Academy.create(
                user,
                application.getAcademyName(),
                application.getRepresentativeName(),
                application.getPhone(),
                application.getPostalCode(),
                application.getAddress(),
                application.getDetailAddress()
        );
        return academyRepository.save(academy);
    }

    @Transactional
    public Academy createForDirectAccount(User user, String name, String phone) {
        validateAcademyUser(user);
        validateAcademyDoesNotExist(user.getId());

        Academy academy = Academy.create(user, name, name, phone, null, null, null);
        return academyRepository.save(academy);
    }

    @Transactional(readOnly = true)
    public AcademyResponse getMyAcademy(Long userId) {
        return AcademyResponse.from(getAcademyForUser(userId));
    }

    @Transactional
    public AcademyResponse updateMyAcademy(Long userId, AcademyUpdateRequest request) {
        Academy academy = getAcademyForUser(userId);
        academy.update(
                request.name(),
                request.representativeName(),
                request.phone(),
                request.postalCode(),
                request.address(),
                request.detailAddress()
        );
        return AcademyResponse.from(academy);
    }

    @Transactional(readOnly = true)
    public AcademyDashboardResponse getMyDashboard(Long userId) {
        Academy academy = getAcademyForUser(userId);
        long studentCount = studentProfileRepository.countByAcademyId(academy.getId());
        long teacherCount = academyMemberRepository.countByAcademyId(academy.getId());
        return AcademyDashboardResponse.of(studentCount, teacherCount);
    }

    private Academy getAcademyForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        validateAcademyUser(user);

        return academyRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ACADEMY_NOT_FOUND));
    }

    private void validateAcademyUser(User user) {
        if (user.getRole() != Role.ACADEMY) {
            throw new BusinessException(ErrorCode.ACADEMY_ACCESS_DENIED);
        }
    }

    private void validateAcademyDoesNotExist(Long userId) {
        if (academyRepository.existsByUserId(userId)) {
            throw new BusinessException(ErrorCode.ACADEMY_ALREADY_EXISTS);
        }
    }
}

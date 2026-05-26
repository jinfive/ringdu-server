package com.ringdu.server.admin.service;

import com.ringdu.server.academy.entity.AcademySignupApplication;
import com.ringdu.server.academy.entity.AcademySignupApplicationStatus;
import com.ringdu.server.academy.repository.AcademySignupApplicationRepository;
import com.ringdu.server.academy.service.AcademyService;
import com.ringdu.server.admin.dto.AcademySignupApplicationResponse;
import com.ringdu.server.admin.dto.AcademySignupApprovalResponse;
import com.ringdu.server.global.exception.BusinessException;
import com.ringdu.server.global.exception.ErrorCode;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminAcademySignupApplicationService {

    private final AcademySignupApplicationRepository academySignupApplicationRepository;
    private final AcademyService academyService;

    @Transactional(readOnly = true)
    public List<AcademySignupApplicationResponse> findPendingApplications() {
        return academySignupApplicationRepository.findAllByStatusOrderByCreatedAtAsc(
                        AcademySignupApplicationStatus.PENDING
                )
                .stream()
                .map(AcademySignupApplicationResponse::from)
                .toList();
    }

    @Transactional
    public AcademySignupApprovalResponse approve(Long applicationId, Long adminUserId) {
        AcademySignupApplication application = academySignupApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ACADEMY_SIGNUP_APPLICATION_NOT_FOUND));

        application.approve(adminUserId, LocalDateTime.now());
        academyService.createFromSignupApplication(application);
        return AcademySignupApprovalResponse.from(application);
    }
}

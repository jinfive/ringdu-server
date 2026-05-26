package com.ringdu.server.admin.controller;

import com.ringdu.server.admin.dto.AcademySignupApplicationResponse;
import com.ringdu.server.admin.dto.AcademySignupApprovalResponse;
import com.ringdu.server.admin.service.AdminAcademySignupApplicationService;
import com.ringdu.server.global.common.ApiResponse;
import com.ringdu.server.global.security.CustomUserPrincipal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/academy-signup-applications")
@RequiredArgsConstructor
public class AdminAcademySignupApplicationController {

    private final AdminAcademySignupApplicationService adminAcademySignupApplicationService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<AcademySignupApplicationResponse>> findPendingApplications() {
        return ApiResponse.success(adminAcademySignupApplicationService.findPendingApplications());
    }

    @PostMapping("/{applicationId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<AcademySignupApprovalResponse> approve(
            @PathVariable Long applicationId,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        AcademySignupApprovalResponse response = adminAcademySignupApplicationService.approve(
                applicationId,
                principal.userId()
        );
        return ApiResponse.success("학원 가입 신청이 승인되었습니다.", response);
    }
}

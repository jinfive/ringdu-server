package com.ringdu.server.academy.controller;

import com.ringdu.server.academy.dto.AcademyDashboardResponse;
import com.ringdu.server.academy.dto.AcademyResponse;
import com.ringdu.server.academy.dto.AcademyUpdateRequest;
import com.ringdu.server.academy.service.AcademyService;
import com.ringdu.server.global.common.ApiResponse;
import com.ringdu.server.global.security.CustomUserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/academies")
@RequiredArgsConstructor
public class AcademyController {

    private final AcademyService academyService;

    @GetMapping("/me")
    @PreAuthorize("hasRole('ACADEMY')")
    public ApiResponse<AcademyResponse> getMyAcademy(@AuthenticationPrincipal CustomUserPrincipal principal) {
        return ApiResponse.success(academyService.getMyAcademy(principal.userId()));
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('ACADEMY')")
    public ApiResponse<AcademyResponse> updateMyAcademy(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody AcademyUpdateRequest request
    ) {
        return ApiResponse.success("학원 정보가 수정되었습니다.", academyService.updateMyAcademy(principal.userId(), request));
    }

    @GetMapping("/me/dashboard")
    @PreAuthorize("hasRole('ACADEMY')")
    public ApiResponse<AcademyDashboardResponse> getMyDashboard(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ApiResponse.success(academyService.getMyDashboard(principal.userId()));
    }
}

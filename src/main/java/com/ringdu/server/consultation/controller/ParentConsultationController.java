package com.ringdu.server.consultation.controller;

import com.ringdu.server.consultation.dto.ConsultationRequestCreateRequest;
import com.ringdu.server.consultation.dto.ConsultationRequestResponse;
import com.ringdu.server.consultation.dto.ParentConsultationDateAvailabilityResponse;
import com.ringdu.server.consultation.dto.ParentConsultationOptionResponse;
import com.ringdu.server.consultation.entity.ConsultationConsultantType;
import com.ringdu.server.consultation.service.ConsultationService;
import com.ringdu.server.global.common.ApiResponse;
import com.ringdu.server.global.security.CustomUserPrincipal;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/parent")
@RequiredArgsConstructor
public class ParentConsultationController {

    private final ConsultationService consultationService;

    @GetMapping("/consultation-options")
    @PreAuthorize("hasRole('PARENT')")
    public ApiResponse<List<ParentConsultationOptionResponse>> getConsultationOptions(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ApiResponse.success(consultationService.getParentConsultationOptions(principal.userId()));
    }

    @GetMapping("/consultation-requests")
    @PreAuthorize("hasRole('PARENT')")
    public ApiResponse<List<ConsultationRequestResponse>> getConsultationRequests(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ApiResponse.success(consultationService.getParentRequests(principal.userId()));
    }

    @GetMapping("/consultation-availability")
    @PreAuthorize("hasRole('PARENT')")
    public ApiResponse<List<ParentConsultationDateAvailabilityResponse>> getConsultationAvailability(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestParam Long academyId,
            @RequestParam(required = false) ConsultationConsultantType consultantType,
            @RequestParam(required = false) Long teacherUserId,
            @RequestParam Integer year,
            @RequestParam Integer month
    ) {
        return ApiResponse.success(consultationService.getParentConsultantAvailability(
                principal.userId(),
                academyId,
                consultantType,
                teacherUserId,
                year,
                month
        ));
    }

    @PostMapping("/consultation-requests")
    @PreAuthorize("hasRole('PARENT')")
    public ApiResponse<ConsultationRequestResponse> createConsultationRequest(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody ConsultationRequestCreateRequest request
    ) {
        return ApiResponse.success(consultationService.createParentRequest(principal.userId(), request));
    }
}

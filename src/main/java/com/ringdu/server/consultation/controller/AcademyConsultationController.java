package com.ringdu.server.consultation.controller;

import com.ringdu.server.consultation.dto.ConsultationAvailabilityRequest;
import com.ringdu.server.consultation.dto.ConsultationAvailabilityResponse;
import com.ringdu.server.consultation.dto.ConsultationRequestActionRequest;
import com.ringdu.server.consultation.dto.ConsultationRequestResponse;
import com.ringdu.server.consultation.entity.ConsultationRequestStatus;
import com.ringdu.server.consultation.entity.ConsultationRequestType;
import com.ringdu.server.consultation.entity.ConsultationType;
import com.ringdu.server.consultation.service.ConsultationService;
import com.ringdu.server.global.common.ApiResponse;
import com.ringdu.server.global.security.CustomUserPrincipal;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/academies")
@RequiredArgsConstructor
public class AcademyConsultationController {

    private final ConsultationService consultationService;

    @GetMapping("/me/consultation-availability")
    @PreAuthorize("hasRole('ACADEMY')")
    public ApiResponse<List<ConsultationAvailabilityResponse>> getMyAvailability(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ApiResponse.success(consultationService.getAcademyAvailability(principal.userId()));
    }

    @PostMapping("/me/consultation-availability")
    @PreAuthorize("hasRole('ACADEMY')")
    public ApiResponse<ConsultationAvailabilityResponse> createAvailability(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody ConsultationAvailabilityRequest request
    ) {
        return ApiResponse.success(consultationService.createAcademyAvailability(principal.userId(), request));
    }

    @PutMapping("/me/consultation-availability/{availabilityId}")
    @PreAuthorize("hasRole('ACADEMY')")
    public ApiResponse<ConsultationAvailabilityResponse> updateAvailability(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long availabilityId,
            @Valid @RequestBody ConsultationAvailabilityRequest request
    ) {
        return ApiResponse.success(consultationService.updateAcademyAvailability(principal.userId(), availabilityId, request));
    }

    @DeleteMapping("/me/consultation-availability/{availabilityId}")
    @PreAuthorize("hasRole('ACADEMY')")
    public ApiResponse<ConsultationAvailabilityResponse> deleteAvailability(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long availabilityId
    ) {
        return ApiResponse.success(consultationService.deleteAcademyAvailability(principal.userId(), availabilityId));
    }

    @GetMapping("/{academyId}/consultation-availability")
    @PreAuthorize("hasAnyRole('PARENT', 'ACADEMY')")
    public ApiResponse<List<ConsultationAvailabilityResponse>> getAcademyAvailability(
            @PathVariable Long academyId,
            @RequestParam(required = false) ConsultationType type
    ) {
        return ApiResponse.success(consultationService.getPublicAvailability(academyId, type));
    }

    @GetMapping("/me/consultation-requests")
    @PreAuthorize("hasRole('ACADEMY')")
    public ApiResponse<List<ConsultationRequestResponse>> getMyConsultationRequests(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestParam(required = false) ConsultationRequestStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) ConsultationRequestType type
    ) {
        return ApiResponse.success(consultationService.getAcademyRequests(principal.userId(), status, from, to, type));
    }

    @PostMapping("/me/consultation-requests/{requestId}/approve")
    @PreAuthorize("hasRole('ACADEMY')")
    public ApiResponse<ConsultationRequestResponse> approveConsultationRequest(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long requestId,
            @RequestBody(required = false) ConsultationRequestActionRequest request
    ) {
        return ApiResponse.success(consultationService.approveAcademyRequest(principal.userId(), requestId, request));
    }

    @PostMapping("/me/consultation-requests/{requestId}/reject")
    @PreAuthorize("hasRole('ACADEMY')")
    public ApiResponse<ConsultationRequestResponse> rejectConsultationRequest(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long requestId,
            @RequestBody(required = false) ConsultationRequestActionRequest request
    ) {
        return ApiResponse.success(consultationService.rejectAcademyRequest(principal.userId(), requestId, request));
    }

    @PostMapping("/me/consultation-requests/{requestId}/complete")
    @PreAuthorize("hasRole('ACADEMY')")
    public ApiResponse<ConsultationRequestResponse> completeConsultationRequest(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long requestId,
            @RequestBody(required = false) ConsultationRequestActionRequest request
    ) {
        return ApiResponse.success(consultationService.completeAcademyRequest(principal.userId(), requestId, request));
    }
}

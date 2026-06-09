package com.ringdu.server.consultation.controller;

import com.ringdu.server.consultation.dto.ConsultationAvailabilityRequest;
import com.ringdu.server.consultation.dto.ConsultationAvailabilityResponse;
import com.ringdu.server.consultation.service.ConsultationService;
import com.ringdu.server.global.common.ApiResponse;
import com.ringdu.server.global.security.CustomUserPrincipal;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/teacher/consultation-availability")
@RequiredArgsConstructor
public class TeacherConsultationAvailabilityController {

    private final ConsultationService consultationService;

    @GetMapping
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResponse<List<ConsultationAvailabilityResponse>> getAvailability(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ApiResponse.success(consultationService.getTeacherAvailability(principal.userId()));
    }

    @PostMapping
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResponse<ConsultationAvailabilityResponse> createAvailability(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody ConsultationAvailabilityRequest request
    ) {
        return ApiResponse.success(consultationService.createTeacherAvailability(principal.userId(), request));
    }

    @PutMapping("/{availabilityId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResponse<ConsultationAvailabilityResponse> updateAvailability(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long availabilityId,
            @Valid @RequestBody ConsultationAvailabilityRequest request
    ) {
        return ApiResponse.success(consultationService.updateTeacherAvailability(
                principal.userId(),
                availabilityId,
                request
        ));
    }

    @DeleteMapping("/{availabilityId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResponse<ConsultationAvailabilityResponse> deleteAvailability(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long availabilityId
    ) {
        return ApiResponse.success(consultationService.deleteTeacherAvailability(principal.userId(), availabilityId));
    }
}

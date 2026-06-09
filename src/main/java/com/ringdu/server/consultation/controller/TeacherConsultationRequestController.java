package com.ringdu.server.consultation.controller;

import com.ringdu.server.consultation.dto.ConsultationRequestActionRequest;
import com.ringdu.server.consultation.dto.ConsultationRequestResponse;
import com.ringdu.server.consultation.entity.ConsultationRequestStatus;
import com.ringdu.server.consultation.service.ConsultationService;
import com.ringdu.server.global.common.ApiResponse;
import com.ringdu.server.global.security.CustomUserPrincipal;
import lombok.RequiredArgsConstructor;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/teacher/consultation-requests")
@RequiredArgsConstructor
public class TeacherConsultationRequestController {

    private final ConsultationService consultationService;

    @GetMapping
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResponse<List<ConsultationRequestResponse>> getConsultationRequests(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestParam(required = false) Long studentProfileId,
            @RequestParam(required = false) ConsultationRequestStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ApiResponse.success(consultationService.getTeacherConsultationRequests(
                principal.userId(), studentProfileId, status, from, to));
    }

    @PostMapping("/{requestId}/complete")
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResponse<ConsultationRequestResponse> completeConsultationRequest(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long requestId,
            @RequestBody(required = false) ConsultationRequestActionRequest request
    ) {
        return ApiResponse.success(consultationService.completeTeacherRequest(principal.userId(), requestId, request));
    }
}

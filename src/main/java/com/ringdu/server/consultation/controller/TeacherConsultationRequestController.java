package com.ringdu.server.consultation.controller;

import com.ringdu.server.consultation.dto.ConsultationRequestActionRequest;
import com.ringdu.server.consultation.dto.ConsultationRequestResponse;
import com.ringdu.server.consultation.service.ConsultationService;
import com.ringdu.server.global.common.ApiResponse;
import com.ringdu.server.global.security.CustomUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/teacher/consultation-requests")
@RequiredArgsConstructor
public class TeacherConsultationRequestController {

    private final ConsultationService consultationService;

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

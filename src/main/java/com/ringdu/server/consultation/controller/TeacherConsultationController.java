package com.ringdu.server.consultation.controller;

import com.ringdu.server.consultation.dto.ConsultationMemoCreateRequest;
import com.ringdu.server.consultation.dto.ConsultationMemoResponse;
import com.ringdu.server.consultation.dto.ConsultationMemoUpdateRequest;
import com.ringdu.server.consultation.dto.TeacherConsultationStudentResponse;
import com.ringdu.server.consultation.service.ConsultationService;
import com.ringdu.server.global.common.ApiResponse;
import com.ringdu.server.global.security.CustomUserPrincipal;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/teacher/consultation-memos")
@RequiredArgsConstructor
public class TeacherConsultationController {

    private final ConsultationService consultationService;

    @GetMapping("/students")
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResponse<List<TeacherConsultationStudentResponse>> getConsultationStudents(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ApiResponse.success(consultationService.getTeacherConsultationStudents(principal.userId()));
    }

    @GetMapping
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResponse<List<ConsultationMemoResponse>> getConsultationMemos(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestParam(required = false) Long studentProfileId
    ) {
        return ApiResponse.success(consultationService.getTeacherMemos(principal.userId(), studentProfileId));
    }

    @PostMapping
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResponse<ConsultationMemoResponse> createConsultationMemo(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody ConsultationMemoCreateRequest request
    ) {
        return ApiResponse.success(consultationService.createTeacherMemo(principal.userId(), request));
    }

    @PutMapping("/{memoId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResponse<ConsultationMemoResponse> updateConsultationMemo(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long memoId,
            @Valid @RequestBody ConsultationMemoUpdateRequest request
    ) {
        return ApiResponse.success(consultationService.updateTeacherMemo(principal.userId(), memoId, request));
    }
}

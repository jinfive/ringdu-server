package com.ringdu.server.academy.controller;

import com.ringdu.server.academy.dto.AcademyTeacherResponse;
import com.ringdu.server.academy.dto.TeacherInvitationCreateRequest;
import com.ringdu.server.academy.dto.TeacherInvitationResponse;
import com.ringdu.server.academy.service.AcademyTeacherInvitationService;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/academies/me")
@RequiredArgsConstructor
public class AcademyTeacherInvitationController {

    private final AcademyTeacherInvitationService academyTeacherInvitationService;

    @PostMapping("/teacher-invitations")
    @PreAuthorize("hasRole('ACADEMY')")
    public ApiResponse<TeacherInvitationResponse> createInvitation(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody TeacherInvitationCreateRequest request
    ) {
        TeacherInvitationResponse response = academyTeacherInvitationService.createInvitation(
                principal.userId(),
                request
        );
        return ApiResponse.success("선생님 초대장을 보냈습니다.", response);
    }

    @GetMapping("/teacher-invitations")
    @PreAuthorize("hasRole('ACADEMY')")
    public ApiResponse<List<TeacherInvitationResponse>> getInvitations(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ApiResponse.success(academyTeacherInvitationService.getMyAcademyInvitations(principal.userId()));
    }

    @PostMapping("/teacher-invitations/{invitationId}/cancel")
    @PreAuthorize("hasRole('ACADEMY')")
    public ApiResponse<TeacherInvitationResponse> cancelInvitation(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long invitationId
    ) {
        return ApiResponse.success(
                "선생님 초대가 취소되었습니다.",
                academyTeacherInvitationService.cancelInvitation(principal.userId(), invitationId)
        );
    }

    @GetMapping("/teachers")
    @PreAuthorize("hasRole('ACADEMY')")
    public ApiResponse<List<AcademyTeacherResponse>> getTeachers(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ApiResponse.success(academyTeacherInvitationService.getMyAcademyTeachers(principal.userId()));
    }
}

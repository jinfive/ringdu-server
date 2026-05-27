package com.ringdu.server.student.controller;

import com.ringdu.server.academy.dto.AcademyStudentInvitationResponse;
import com.ringdu.server.academy.service.AcademyStudentInvitationService;
import com.ringdu.server.global.common.ApiResponse;
import com.ringdu.server.global.security.CustomUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student/academy-invitations")
@RequiredArgsConstructor
public class StudentAcademyInvitationController {

    private final AcademyStudentInvitationService invitationService;

    @GetMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ApiResponse<List<AcademyStudentInvitationResponse>> getInvitations(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ApiResponse.success(invitationService.getStudentInvitations(principal.userId()));
    }

    @PostMapping("/{invitationId}/accept")
    @PreAuthorize("hasRole('STUDENT')")
    public ApiResponse<String> acceptInvitation(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long invitationId
    ) {
        invitationService.acceptInvitation(principal.userId(), invitationId);
        return ApiResponse.success("초대를 수락했습니다.");
    }

    @PostMapping("/{invitationId}/reject")
    @PreAuthorize("hasRole('STUDENT')")
    public ApiResponse<String> rejectInvitation(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long invitationId
    ) {
        invitationService.rejectInvitation(principal.userId(), invitationId);
        return ApiResponse.success("초대를 거절했습니다.");
    }
}

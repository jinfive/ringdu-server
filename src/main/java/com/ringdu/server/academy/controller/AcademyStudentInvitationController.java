package com.ringdu.server.academy.controller;

import com.ringdu.server.academy.dto.AcademyStudentInvitationCreateRequest;
import com.ringdu.server.academy.dto.AcademyStudentInvitationResponse;
import com.ringdu.server.academy.service.AcademyStudentInvitationService;
import com.ringdu.server.global.common.ApiResponse;
import com.ringdu.server.global.security.CustomUserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/academies/me/student-invitations")
@RequiredArgsConstructor
public class AcademyStudentInvitationController {

    private final AcademyStudentInvitationService invitationService;

    @PostMapping
    @PreAuthorize("hasRole('ACADEMY')")
    public ApiResponse<AcademyStudentInvitationResponse> createInvitation(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody AcademyStudentInvitationCreateRequest request
    ) {
        return ApiResponse.success("초대장이 발송되었습니다.", invitationService.createInvitation(principal.userId(), request));
    }

    @GetMapping
    @PreAuthorize("hasRole('ACADEMY')")
    public ApiResponse<List<AcademyStudentInvitationResponse>> getInvitations(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ApiResponse.success(invitationService.getAcademyInvitations(principal.userId()));
    }
}

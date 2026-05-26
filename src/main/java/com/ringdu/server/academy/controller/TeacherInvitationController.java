package com.ringdu.server.academy.controller;

import com.ringdu.server.academy.dto.MyTeacherInvitationResponse;
import com.ringdu.server.academy.service.AcademyTeacherInvitationService;
import com.ringdu.server.global.common.ApiResponse;
import com.ringdu.server.global.security.CustomUserPrincipal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/teacher/invitations")
@RequiredArgsConstructor
public class TeacherInvitationController {

    private final AcademyTeacherInvitationService academyTeacherInvitationService;

    @GetMapping
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResponse<List<MyTeacherInvitationResponse>> getMyInvitations(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ApiResponse.success(academyTeacherInvitationService.getMyTeacherInvitations(principal.userId()));
    }

    @PostMapping("/{invitationId}/accept")
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResponse<MyTeacherInvitationResponse> accept(
            @PathVariable Long invitationId,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        MyTeacherInvitationResponse response = academyTeacherInvitationService.acceptInvitation(
                principal.userId(),
                invitationId
        );
        return ApiResponse.success("학원 초대를 수락했습니다.", response);
    }

    @PostMapping("/{invitationId}/reject")
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResponse<MyTeacherInvitationResponse> reject(
            @PathVariable Long invitationId,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        MyTeacherInvitationResponse response = academyTeacherInvitationService.rejectInvitation(
                principal.userId(),
                invitationId
        );
        return ApiResponse.success("학원 초대를 거절했습니다.", response);
    }
}

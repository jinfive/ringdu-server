package com.ringdu.server.parentstudent.controller;

import com.ringdu.server.global.common.ApiResponse;
import com.ringdu.server.global.security.CustomUserPrincipal;
import com.ringdu.server.parentstudent.dto.ParentStudentInvitationCreateRequest;
import com.ringdu.server.parentstudent.dto.ParentStudentInvitationResponse;
import com.ringdu.server.parentstudent.dto.ParentStudentRelationResponse;
import com.ringdu.server.parentstudent.dto.StudentParentInvitationCreateRequest;
import com.ringdu.server.parentstudent.service.ParentStudentInvitationService;
import com.ringdu.server.user.entity.Role;
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
@RequiredArgsConstructor
public class ParentStudentInvitationController {

    private final ParentStudentInvitationService invitationService;

    @PostMapping("/api/parent/student-invitations")
    @PreAuthorize("hasRole('PARENT')")
    public ApiResponse<ParentStudentInvitationResponse> createStudentInvitation(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody ParentStudentInvitationCreateRequest request
    ) {
        ParentStudentInvitationResponse response = invitationService.createStudentInvitation(
                principal.userId(),
                request
        );
        return ApiResponse.success("자녀 연결 요청을 보냈습니다.", response);
    }

    @PostMapping("/api/student/parent-invitations")
    @PreAuthorize("hasRole('STUDENT')")
    public ApiResponse<ParentStudentInvitationResponse> createParentInvitation(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody StudentParentInvitationCreateRequest request
    ) {
        ParentStudentInvitationResponse response = invitationService.createParentInvitation(
                principal.userId(),
                request
        );
        return ApiResponse.success("보호자 연결 요청을 보냈습니다.", response);
    }

    @GetMapping("/api/parent/invitations")
    @PreAuthorize("hasRole('PARENT')")
    public ApiResponse<List<ParentStudentInvitationResponse>> getParentInvitations(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ApiResponse.success(invitationService.getMyInvitations(principal.userId(), Role.PARENT));
    }

    @GetMapping("/api/student/invitations")
    @PreAuthorize("hasRole('STUDENT')")
    public ApiResponse<List<ParentStudentInvitationResponse>> getStudentInvitations(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ApiResponse.success(invitationService.getMyInvitations(principal.userId(), Role.STUDENT));
    }

    @GetMapping("/api/parent/students")
    @PreAuthorize("hasRole('PARENT')")
    public ApiResponse<List<ParentStudentRelationResponse>> getParentStudents(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ApiResponse.success(invitationService.getMyChildren(principal.userId()));
    }

    @GetMapping("/api/student/parents")
    @PreAuthorize("hasRole('STUDENT')")
    public ApiResponse<List<ParentStudentRelationResponse>> getStudentParents(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ApiResponse.success(invitationService.getMyParents(principal.userId()));
    }

    @PostMapping("/api/parent-student-invitations/{invitationId}/accept")
    @PreAuthorize("hasAnyRole('PARENT', 'STUDENT')")
    public ApiResponse<ParentStudentInvitationResponse> accept(
            @PathVariable Long invitationId,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        ParentStudentInvitationResponse response = invitationService.acceptInvitation(
                principal.userId(),
                invitationId
        );
        return ApiResponse.success("연결 초대를 수락했습니다.", response);
    }

    @PostMapping("/api/parent-student-invitations/{invitationId}/reject")
    @PreAuthorize("hasAnyRole('PARENT', 'STUDENT')")
    public ApiResponse<ParentStudentInvitationResponse> reject(
            @PathVariable Long invitationId,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        ParentStudentInvitationResponse response = invitationService.rejectInvitation(
                principal.userId(),
                invitationId
        );
        return ApiResponse.success("연결 초대를 거절했습니다.", response);
    }
}

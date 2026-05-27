package com.ringdu.server.academy.controller;

import com.ringdu.server.academy.dto.AccountCandidateResponse;
import com.ringdu.server.academy.service.AcademyAccountCandidateService;
import com.ringdu.server.global.common.ApiResponse;
import com.ringdu.server.user.entity.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/academies/me/account-candidates")
@RequiredArgsConstructor
public class AcademyAccountCandidateController {

    private final AcademyAccountCandidateService candidateService;

    @GetMapping
    @PreAuthorize("hasRole('ACADEMY')")
    public ApiResponse<AccountCandidateResponse> searchCandidates(
            @RequestParam Role role,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phone
    ) {
        return ApiResponse.success(candidateService.searchCandidates(role, email, phone));
    }
}

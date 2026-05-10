package com.ringdu.server.admin.controller;

import com.ringdu.server.admin.dto.AcademyAccountResponse;
import com.ringdu.server.admin.dto.CreateAcademyAccountRequest;
import com.ringdu.server.admin.service.AdminAcademyAccountService;
import com.ringdu.server.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/academy-accounts")
@RequiredArgsConstructor
public class AdminAcademyAccountController {

    private final AdminAcademyAccountService adminAcademyAccountService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<AcademyAccountResponse> createAcademyAccount(
            @Valid @RequestBody CreateAcademyAccountRequest request
    ) {
        AcademyAccountResponse response = adminAcademyAccountService.createAcademyAccount(request);
        return ApiResponse.success("학원 계정이 생성되었습니다.", response);
    }
}

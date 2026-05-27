package com.ringdu.server.student.controller;

import com.ringdu.server.global.common.ApiResponse;
import com.ringdu.server.global.security.CustomUserPrincipal;
import com.ringdu.server.student.dto.AcademyStudentCreateRequest;
import com.ringdu.server.student.dto.AcademyStudentResponse;
import com.ringdu.server.student.service.AcademyStudentService;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/academies/me/students")
@RequiredArgsConstructor
public class AcademyStudentController {

    private final AcademyStudentService academyStudentService;

    @PostMapping
    @PreAuthorize("hasRole('ACADEMY')")
    public ApiResponse<AcademyStudentResponse> createStudent(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody AcademyStudentCreateRequest request
    ) {
        return ApiResponse.success("학생 정보가 등록되었습니다.", academyStudentService.createStudent(principal.userId(), request));
    }

    @GetMapping
    @PreAuthorize("hasRole('ACADEMY')")
    public ApiResponse<List<AcademyStudentResponse>> getStudents(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ApiResponse.success(academyStudentService.getMyAcademyStudents(principal.userId()));
    }

    @GetMapping("/{studentId}")
    @PreAuthorize("hasRole('ACADEMY')")
    public ApiResponse<AcademyStudentResponse> getStudent(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long studentId
    ) {
        return ApiResponse.success(academyStudentService.getStudent(principal.userId(), studentId));
    }

    @PutMapping("/{studentId}")
    @PreAuthorize("hasRole('ACADEMY')")
    public ApiResponse<AcademyStudentResponse> updateStudent(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long studentId,
            @Valid @RequestBody AcademyStudentCreateRequest request
    ) {
        return ApiResponse.success("학생 정보가 수정되었습니다.", academyStudentService.updateStudent(principal.userId(), studentId, request));
    }
}

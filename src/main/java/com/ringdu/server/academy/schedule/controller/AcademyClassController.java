package com.ringdu.server.academy.schedule.controller;

import com.ringdu.server.academy.schedule.dto.AcademyClassDetailResponse;
import com.ringdu.server.academy.schedule.dto.AcademyClassRequest;
import com.ringdu.server.academy.schedule.dto.AcademyClassResponse;
import com.ringdu.server.academy.schedule.dto.AcademyClassStudentRequest;
import com.ringdu.server.academy.schedule.entity.AcademyClassDayOfWeek;
import com.ringdu.server.academy.schedule.entity.ScheduleStatus;
import com.ringdu.server.academy.schedule.service.AcademyScheduleService;
import com.ringdu.server.global.common.ApiResponse;
import com.ringdu.server.global.security.CustomUserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/academies/me/classes")
@RequiredArgsConstructor
public class AcademyClassController {

    private final AcademyScheduleService academyScheduleService;

    @GetMapping
    @PreAuthorize("hasRole('ACADEMY')")
    public ApiResponse<List<AcademyClassResponse>> getClasses(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestParam(required = false) AcademyClassDayOfWeek dayOfWeek,
            @RequestParam(required = false) Long classroomId,
            @RequestParam(required = false) ScheduleStatus status
    ) {
        return ApiResponse.success(academyScheduleService.getClasses(principal.userId(), dayOfWeek, classroomId, status));
    }

    @PostMapping
    @PreAuthorize("hasRole('ACADEMY')")
    public ApiResponse<AcademyClassResponse> createClass(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody AcademyClassRequest request
    ) {
        return ApiResponse.success("수업이 생성되었습니다.", academyScheduleService.createClass(principal.userId(), request));
    }

    @GetMapping("/{classId}")
    @PreAuthorize("hasRole('ACADEMY')")
    public ApiResponse<AcademyClassDetailResponse> getClass(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long classId
    ) {
        return ApiResponse.success(academyScheduleService.getClass(principal.userId(), classId));
    }

    @PutMapping("/{classId}")
    @PreAuthorize("hasRole('ACADEMY')")
    public ApiResponse<AcademyClassResponse> updateClass(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long classId,
            @Valid @RequestBody AcademyClassRequest request
    ) {
        return ApiResponse.success("수업이 수정되었습니다.", academyScheduleService.updateClass(principal.userId(), classId, request));
    }

    @DeleteMapping("/{classId}")
    @PreAuthorize("hasRole('ACADEMY')")
    public ApiResponse<Void> deleteClass(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long classId
    ) {
        academyScheduleService.deleteClass(principal.userId(), classId);
        return ApiResponse.success("수업이 비활성화되었습니다.", null);
    }

    @PostMapping("/{classId}/students")
    @PreAuthorize("hasRole('ACADEMY')")
    public ApiResponse<AcademyClassDetailResponse> addStudent(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long classId,
            @Valid @RequestBody AcademyClassStudentRequest request
    ) {
        return ApiResponse.success("수강 학생이 추가되었습니다.", academyScheduleService.addStudent(principal.userId(), classId, request.studentProfileId()));
    }

    @DeleteMapping("/{classId}/students/{studentProfileId}")
    @PreAuthorize("hasRole('ACADEMY')")
    public ApiResponse<Void> deleteStudent(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long classId,
            @PathVariable Long studentProfileId
    ) {
        academyScheduleService.deleteStudent(principal.userId(), classId, studentProfileId);
        return ApiResponse.success("수강 학생이 비활성화되었습니다.", null);
    }
}

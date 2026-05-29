package com.ringdu.server.academy.schedule.controller;

import com.ringdu.server.academy.schedule.dto.AcademyClassroomCreateRequest;
import com.ringdu.server.academy.schedule.dto.AcademyClassroomResponse;
import com.ringdu.server.academy.schedule.dto.AcademyClassroomUpdateRequest;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/academies/me/classrooms")
@RequiredArgsConstructor
public class AcademyClassroomController {

    private final AcademyScheduleService academyScheduleService;

    @GetMapping
    @PreAuthorize("hasRole('ACADEMY')")
    public ApiResponse<List<AcademyClassroomResponse>> getClassrooms(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ApiResponse.success(academyScheduleService.getClassrooms(principal.userId()));
    }

    @PostMapping
    @PreAuthorize("hasRole('ACADEMY')")
    public ApiResponse<AcademyClassroomResponse> createClassroom(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody AcademyClassroomCreateRequest request
    ) {
        return ApiResponse.success("강의실이 생성되었습니다.", academyScheduleService.createClassroom(principal.userId(), request));
    }

    @PutMapping("/{classroomId}")
    @PreAuthorize("hasRole('ACADEMY')")
    public ApiResponse<AcademyClassroomResponse> updateClassroom(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long classroomId,
            @Valid @RequestBody AcademyClassroomUpdateRequest request
    ) {
        return ApiResponse.success("강의실이 수정되었습니다.", academyScheduleService.updateClassroom(principal.userId(), classroomId, request));
    }

    @DeleteMapping("/{classroomId}")
    @PreAuthorize("hasRole('ACADEMY')")
    public ApiResponse<Void> deleteClassroom(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long classroomId
    ) {
        academyScheduleService.deleteClassroom(principal.userId(), classroomId);
        return ApiResponse.success("강의실이 비활성화되었습니다.", null);
    }
}

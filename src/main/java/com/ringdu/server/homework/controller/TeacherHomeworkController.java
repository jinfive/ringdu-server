package com.ringdu.server.homework.controller;

import com.ringdu.server.global.common.ApiResponse;
import com.ringdu.server.global.security.CustomUserPrincipal;
import com.ringdu.server.homework.dto.HomeworkCreateRequest;
import com.ringdu.server.homework.dto.HomeworkDetailResponse;
import com.ringdu.server.homework.dto.HomeworkStudentStatusUpdateRequest;
import com.ringdu.server.homework.dto.HomeworkSummaryResponse;
import com.ringdu.server.homework.dto.TeacherHomeworkClassResponse;
import com.ringdu.server.homework.service.HomeworkService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TeacherHomeworkController {

    private final HomeworkService homeworkService;

    @GetMapping("/api/teacher/classes")
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResponse<List<TeacherHomeworkClassResponse>> getTeacherClasses(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ApiResponse.success(homeworkService.getTeacherClasses(principal.userId()));
    }

    @GetMapping("/api/teacher/classes/{classId}/homeworks")
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResponse<List<HomeworkSummaryResponse>> getClassHomeworks(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long classId
    ) {
        return ApiResponse.success(homeworkService.getTeacherClassHomeworks(principal.userId(), classId));
    }

    @PostMapping("/api/teacher/classes/{classId}/homeworks")
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResponse<HomeworkDetailResponse> createClassHomework(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long classId,
            @Valid @RequestBody HomeworkCreateRequest request
    ) {
        return ApiResponse.success("숙제가 등록되었습니다.", homeworkService.createTeacherClassHomework(principal.userId(), classId, request));
    }

    @GetMapping("/api/teacher/classes/{classId}/homeworks/{homeworkId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResponse<HomeworkDetailResponse> getClassHomework(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long classId,
            @PathVariable Long homeworkId
    ) {
        return ApiResponse.success(homeworkService.getTeacherClassHomeworkDetail(principal.userId(), classId, homeworkId));
    }

    @DeleteMapping("/api/teacher/classes/{classId}/homeworks/{homeworkId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResponse<Void> deleteClassHomework(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long classId,
            @PathVariable Long homeworkId
    ) {
        homeworkService.deleteTeacherClassHomework(principal.userId(), classId, homeworkId);
        return ApiResponse.success("숙제가 삭제되었습니다.", null);
    }

    @PatchMapping("/api/teacher/homework-students/{homeworkStudentId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResponse<HomeworkDetailResponse> updateHomeworkStudent(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long homeworkStudentId,
            @Valid @RequestBody HomeworkStudentStatusUpdateRequest request
    ) {
        return ApiResponse.success("숙제 상태가 저장되었습니다.", homeworkService.updateTeacherHomeworkStudent(principal.userId(), homeworkStudentId, request));
    }
}

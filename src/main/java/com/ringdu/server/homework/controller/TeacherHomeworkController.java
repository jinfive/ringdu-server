package com.ringdu.server.homework.controller;

import com.ringdu.server.global.common.ApiResponse;
import com.ringdu.server.global.security.CustomUserPrincipal;
import com.ringdu.server.homework.dto.HomeworkCreateRequest;
import com.ringdu.server.homework.dto.HomeworkDetailResponse;
import com.ringdu.server.homework.dto.HomeworkStudentUpdateRequest;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/teacher")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TEACHER')")
public class TeacherHomeworkController {

    private final HomeworkService homeworkService;

    @GetMapping("/classes")
    public ApiResponse<List<TeacherHomeworkClassResponse>> getClasses(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ApiResponse.success(homeworkService.getTeacherClasses(principal.userId()));
    }

    @GetMapping("/classes/{classId}/homeworks")
    public ApiResponse<List<HomeworkSummaryResponse>> getHomeworks(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long classId
    ) {
        return ApiResponse.success(homeworkService.getTeacherClassHomeworks(principal.userId(), classId));
    }

    @PostMapping("/classes/{classId}/homeworks")
    public ApiResponse<HomeworkDetailResponse> createHomework(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long classId,
            @Valid @RequestBody HomeworkCreateRequest request
    ) {
        return ApiResponse.success(
                "숙제가 등록되었습니다.",
                homeworkService.createTeacherHomework(principal.userId(), classId, request)
        );
    }

    @GetMapping("/classes/{classId}/homeworks/{homeworkId}")
    public ApiResponse<HomeworkDetailResponse> getHomeworkDetail(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long classId,
            @PathVariable Long homeworkId
    ) {
        return ApiResponse.success(
                homeworkService.getTeacherHomeworkDetail(principal.userId(), classId, homeworkId)
        );
    }

    @PatchMapping("/homework-students/{homeworkStudentId}")
    public ApiResponse<HomeworkDetailResponse> updateHomeworkStudent(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long homeworkStudentId,
            @Valid @RequestBody HomeworkStudentUpdateRequest request
    ) {
        return ApiResponse.success(
                "숙제 상태가 변경되었습니다.",
                homeworkService.updateTeacherHomeworkStudent(principal.userId(), homeworkStudentId, request)
        );
    }

    @DeleteMapping("/classes/{classId}/homeworks/{homeworkId}")
    public ApiResponse<Void> deleteHomework(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long classId,
            @PathVariable Long homeworkId
    ) {
        homeworkService.deleteTeacherHomework(principal.userId(), classId, homeworkId);
        return ApiResponse.success("숙제가 삭제되었습니다.", null);
    }
}

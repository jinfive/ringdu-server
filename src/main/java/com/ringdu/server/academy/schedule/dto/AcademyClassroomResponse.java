package com.ringdu.server.academy.schedule.dto;

import com.ringdu.server.academy.schedule.entity.AcademyClassroom;
import com.ringdu.server.academy.schedule.entity.ScheduleStatus;

public record AcademyClassroomResponse(
        Long classroomId,
        String name,
        ScheduleStatus status,
        int displayOrder
) {

    public static AcademyClassroomResponse from(AcademyClassroom classroom) {
        return new AcademyClassroomResponse(
                classroom.getId(),
                classroom.getName(),
                classroom.getStatus(),
                classroom.getDisplayOrder()
        );
    }
}

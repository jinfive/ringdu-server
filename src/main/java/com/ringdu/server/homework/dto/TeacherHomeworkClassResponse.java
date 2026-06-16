package com.ringdu.server.homework.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ringdu.server.academy.schedule.entity.AcademyClass;
import com.ringdu.server.academy.schedule.entity.AcademyClassroom;
import com.ringdu.server.student.entity.StudentProfile;
import java.time.LocalTime;
import java.util.List;

public record TeacherHomeworkClassResponse(
        Long classId,
        Long academyId,
        String academyName,
        String className,
        String dayOfWeek,
        String dayLabel,
        @JsonFormat(pattern = "HH:mm")
        LocalTime startTime,
        @JsonFormat(pattern = "HH:mm")
        LocalTime endTime,
        String classroomName,
        int studentCount,
        List<StudentItem> students
) {
    public static TeacherHomeworkClassResponse of(
            AcademyClass academyClass,
            String academyName,
            AcademyClassroom classroom,
            List<StudentProfile> students
    ) {
        return new TeacherHomeworkClassResponse(
                academyClass.getId(),
                academyClass.getAcademyId(),
                academyName,
                academyClass.getName(),
                academyClass.getDayOfWeek().name(),
                academyClass.getDayOfWeek().getLabel(),
                academyClass.getStartTime(),
                academyClass.getEndTime(),
                classroom.getName(),
                students.size(),
                students.stream().map(StudentItem::from).toList()
        );
    }

    public record StudentItem(Long studentProfileId, String studentName) {
        public static StudentItem from(StudentProfile studentProfile) {
            return new StudentItem(studentProfile.getId(), studentProfile.getName());
        }
    }
}

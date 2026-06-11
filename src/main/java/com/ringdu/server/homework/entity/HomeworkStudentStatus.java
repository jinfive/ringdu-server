package com.ringdu.server.homework.entity;

import lombok.Getter;

@Getter
public enum HomeworkStudentStatus {
    DONE("해옴"),
    NOT_DONE("안해옴");

    private final String label;

    HomeworkStudentStatus(String label) {
        this.label = label;
    }
}

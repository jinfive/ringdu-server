package com.ringdu.server.academy.schedule.entity;

public enum AcademyClassDayOfWeek {
    MONDAY("월"),
    TUESDAY("화"),
    WEDNESDAY("수"),
    THURSDAY("목"),
    FRIDAY("금"),
    SATURDAY("토"),
    SUNDAY("일");

    private final String label;

    AcademyClassDayOfWeek(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}

package com.project.appointment.entity;

public enum DayOfWeek {
    MONDAY,
    TUESDAY,
    WEDNESDAY,
    THURSDAY,
    FRIDAY,
    SATURDAY,
    SUNDAY;
    
    public static DayOfWeek fromJavaTime(java.time.DayOfWeek javaDayOfWeek) {
        return DayOfWeek.valueOf(javaDayOfWeek.name());
    }
}


package com.edutrack.model;

/**
 * Enumeration representing the status of a course enrollment.
 */
public enum EnrollmentStatus {
    ACTIVE("Active"),
    COMPLETED("Completed"),
    DROPPED("Dropped");

    private final String display;

    EnrollmentStatus(String display) {
        this.display = display;
    }

    public String getDisplay() {
        return display;
    }

    public static EnrollmentStatus fromString(String text) {
        if (text == null) return ACTIVE;
        for (EnrollmentStatus s : values()) {
            if (s.name().equalsIgnoreCase(text.trim()) || s.display.equalsIgnoreCase(text.trim())) {
                return s;
            }
        }
        return ACTIVE;
    }

    @Override
    public String toString() {
        return display;
    }
}

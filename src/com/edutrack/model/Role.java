package com.edutrack.model;

/**
 * Enumeration representing the user roles within the EduTrack system.
 */
public enum Role {
    ADMIN("Administrator"),
    INSTRUCTOR("Course Instructor"),
    STUDENT("Student");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}

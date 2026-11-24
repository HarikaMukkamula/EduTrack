package com.edutrack.exception;

/**
 * Thrown when a student attempts to enroll in a course they are already actively enrolled in.
 */
public class DuplicateEnrollmentException extends EduTrackException {
    public DuplicateEnrollmentException(String message) {
        super(message);
    }
}

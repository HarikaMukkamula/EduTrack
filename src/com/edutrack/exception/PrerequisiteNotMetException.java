package com.edutrack.exception;

/**
 * Thrown when a student attempts to enroll in a course without passing its required prerequisites.
 */
public class PrerequisiteNotMetException extends EduTrackException {
    public PrerequisiteNotMetException(String message) {
        super(message);
    }
}

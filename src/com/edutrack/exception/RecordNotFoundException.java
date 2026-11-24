package com.edutrack.exception;

/**
 * Thrown when a queried entity (User, Course, Enrollment) is not found in the DataStore.
 */
public class RecordNotFoundException extends EduTrackException {
    public RecordNotFoundException(String message) {
        super(message);
    }
}

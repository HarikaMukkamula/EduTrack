package com.edutrack.exception;

/**
 * Base custom checked exception for the EduTrack application.
 */
public class EduTrackException extends Exception {
    public EduTrackException(String message) {
        super(message);
    }

    public EduTrackException(String message, Throwable cause) {
        super(message, cause);
    }
}

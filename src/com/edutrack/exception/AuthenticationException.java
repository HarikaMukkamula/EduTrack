package com.edutrack.exception;

/**
 * Thrown when user login credentials fail or access is unauthorized.
 */
public class AuthenticationException extends EduTrackException {
    public AuthenticationException(String message) {
        super(message);
    }
}

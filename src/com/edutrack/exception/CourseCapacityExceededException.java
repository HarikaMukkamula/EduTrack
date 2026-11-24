package com.edutrack.exception;

/**
 * Thrown when an enrollment attempt exceeds the maximum seat capacity of a course.
 */
public class CourseCapacityExceededException extends EduTrackException {
    public CourseCapacityExceededException(String message) {
        super(message);
    }
}

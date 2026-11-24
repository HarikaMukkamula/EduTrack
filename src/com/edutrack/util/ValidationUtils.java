package com.edutrack.util;

import java.util.regex.Pattern;

/**
 * Utility methods for input validation across the application.
 */
public final class ValidationUtils {
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern COURSE_CODE_PATTERN =
            Pattern.compile("^[A-Za-z]{2,5}[0-9]{3,4}[A-Za-z]?$");

    private ValidationUtils() {}

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static boolean isValidCourseCode(String code) {
        return code != null && COURSE_CODE_PATTERN.matcher(code.trim()).matches();
    }

    public static boolean isNotEmpty(String text) {
        return text != null && !text.trim().isEmpty();
    }
}

package com.edutrack.model;

/**
 * Enumeration representing academic letter grades, their grade points (on a standard 4.0 scale),
 * and description.
 */
public enum Grade {
    A_PLUS("A+", 4.0, "Outstanding"),
    A("A", 4.0, "Excellent"),
    A_MINUS("A-", 3.7, "Very Good"),
    B_PLUS("B+", 3.3, "Good"),
    B("B", 3.0, "Above Average"),
    B_MINUS("B-", 2.7, "Average"),
    C_PLUS("C+", 2.3, "Satisfactory"),
    C("C", 2.0, "Pass"),
    D("D", 1.0, "Marginal Pass"),
    F("F", 0.0, "Fail"),
    IN_PROGRESS("IP", -1.0, "Course In Progress");

    private final String letter;
    private final double gradePoint;
    private final String description;

    Grade(String letter, double gradePoint, String description) {
        this.letter = letter;
        this.gradePoint = gradePoint;
        this.description = description;
    }

    public String getLetter() {
        return letter;
    }

    public double getGradePoint() {
        return gradePoint;
    }

    public String getDescription() {
        return description;
    }

    public boolean isPassing() {
        return this != F && this != IN_PROGRESS;
    }

    /**
     * Converts numeric score (0 - 100) to corresponding letter Grade.
     */
    public static Grade fromScore(double score) {
        if (score >= 90.0) return A;
        if (score >= 80.0) return B;
        if (score >= 70.0) return C;
        if (score >= 60.0) return D;
        return F;
    }

    public static Grade fromString(String text) {
        if (text == null || text.trim().isEmpty()) {
            return IN_PROGRESS;
        }
        String clean = text.trim().toUpperCase();
        for (Grade g : values()) {
            if (g.letter.equalsIgnoreCase(clean) || g.name().equalsIgnoreCase(clean)) {
                return g;
            }
        }
        return IN_PROGRESS;
    }

    @Override
    public String toString() {
        return letter;
    }
}

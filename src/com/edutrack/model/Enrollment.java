package com.edutrack.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Represents a student's enrollment in a course.
 * Demonstrates:
 * - Association / Aggregation: Connects Student and Course with enrollment metadata.
 * - Encapsulation: Validates grading, scores, and status transitions.
 */
public class Enrollment implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final String enrollmentId;
    private final String studentId;
    private final String courseCode;
    private String semester;
    private double score; // 0.0 - 100.0, -1.0 means ungraded
    private Grade grade;
    private EnrollmentStatus status;
    private String enrollmentDate;

    public Enrollment(String enrollmentId, String studentId, String courseCode, String semester) {
        this(enrollmentId, studentId, courseCode, semester, -1.0, Grade.IN_PROGRESS,
                EnrollmentStatus.ACTIVE, LocalDateTime.now().format(FORMATTER));
    }

    public Enrollment(String enrollmentId, String studentId, String courseCode, String semester,
                      double score, Grade grade, EnrollmentStatus status, String enrollmentDate) {
        if (enrollmentId == null || enrollmentId.trim().isEmpty()) {
            throw new IllegalArgumentException("Enrollment ID cannot be empty.");
        }
        if (studentId == null || studentId.trim().isEmpty()) {
            throw new IllegalArgumentException("Student ID cannot be empty.");
        }
        if (courseCode == null || courseCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Course code cannot be empty.");
        }

        this.enrollmentId = enrollmentId.trim();
        this.studentId = studentId.trim();
        this.courseCode = courseCode.trim().toUpperCase();
        this.semester = (semester == null || semester.trim().isEmpty()) ? "Fall 2026" : semester.trim();
        this.score = score;
        this.grade = (grade == null) ? Grade.IN_PROGRESS : grade;
        this.status = (status == null) ? EnrollmentStatus.ACTIVE : status;
        this.enrollmentDate = (enrollmentDate == null) ? LocalDateTime.now().format(FORMATTER) : enrollmentDate;
    }

    public String getEnrollmentId() {
        return enrollmentId;
    }

    public String getStudentId() {
        return studentId;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public double getScore() {
        return score;
    }

    public Grade getGrade() {
        return grade;
    }

    public EnrollmentStatus getStatus() {
        return status;
    }

    public void setStatus(EnrollmentStatus status) {
        this.status = status;
    }

    public String getEnrollmentDate() {
        return enrollmentDate;
    }

    public boolean isGraded() {
        return grade != null && grade != Grade.IN_PROGRESS;
    }

    public void recordGrade(double score, Grade grade) {
        if (score < 0.0 || score > 100.0) {
            throw new IllegalArgumentException("Score must be between 0.0 and 100.0.");
        }
        this.score = score;
        this.grade = (grade != null) ? grade : Grade.fromScore(score);
        this.status = EnrollmentStatus.COMPLETED;
    }

    public void recordScore(double score) {
        if (score < 0.0 || score > 100.0) {
            throw new IllegalArgumentException("Score must be between 0.0 and 100.0.");
        }
        this.score = score;
        this.grade = Grade.fromScore(score);
        this.status = EnrollmentStatus.COMPLETED;
    }

    public void drop() {
        this.status = EnrollmentStatus.DROPPED;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Enrollment that = (Enrollment) o;
        return Objects.equals(enrollmentId, that.enrollmentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(enrollmentId);
    }

    @Override
    public String toString() {
        return String.format("[%s] Student: %s -> Course: %s | Sem: %s | Status: %s | Grade: %s (%.1f)",
                enrollmentId, studentId, courseCode, semester, status, grade, score >= 0 ? score : 0.0);
    }
}

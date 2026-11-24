package com.edutrack.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Represents an academic Course in the EduTrack system.
 * Demonstrates:
 * - Encapsulation: Protects internal state (capacity, prerequisites, credits).
 * - Validation: Ensures valid credit bounds (1-6) and positive capacity.
 */
public class Course implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String courseCode;
    private String title;
    private String description;
    private int credits;
    private String department;
    private int maxCapacity;
    private String instructorId; // ID of the instructor assigned
    private final Set<String> prerequisites;

    public Course(String courseCode, String title, String description, int credits,
                  String department, int maxCapacity, String instructorId) {
        if (courseCode == null || courseCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Course code cannot be empty.");
        }
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Course title cannot be empty.");
        }
        if (credits <= 0 || credits > 8) {
            throw new IllegalArgumentException("Credits must be between 1 and 8.");
        }
        if (maxCapacity <= 0) {
            throw new IllegalArgumentException("Maximum capacity must be greater than zero.");
        }

        this.courseCode = courseCode.trim().toUpperCase();
        this.title = title.trim();
        this.description = (description == null) ? "" : description.trim();
        this.credits = credits;
        this.department = (department == null || department.trim().isEmpty()) ? "Computer Science" : department.trim();
        this.maxCapacity = maxCapacity;
        this.instructorId = (instructorId == null || instructorId.trim().isEmpty()) ? null : instructorId.trim();
        this.prerequisites = new HashSet<>();
    }

    public String getCourseCode() {
        return courseCode;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be empty.");
        }
        this.title = title.trim();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = (description == null) ? "" : description.trim();
    }

    public int getCredits() {
        return credits;
    }

    public void setCredits(int credits) {
        if (credits <= 0 || credits > 8) {
            throw new IllegalArgumentException("Credits must be between 1 and 8.");
        }
        this.credits = credits;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    public void setMaxCapacity(int maxCapacity) {
        if (maxCapacity <= 0) {
            throw new IllegalArgumentException("Capacity must be positive.");
        }
        this.maxCapacity = maxCapacity;
    }

    public String getInstructorId() {
        return instructorId;
    }

    public void setInstructorId(String instructorId) {
        this.instructorId = (instructorId == null || instructorId.trim().isEmpty()) ? null : instructorId.trim();
    }

    public boolean hasAssignedInstructor() {
        return instructorId != null && !instructorId.trim().isEmpty();
    }

    public Set<String> getPrerequisites() {
        return Collections.unmodifiableSet(prerequisites);
    }

    public void addPrerequisite(String prerequisiteCode) {
        if (prerequisiteCode != null && !prerequisiteCode.trim().isEmpty()) {
            String clean = prerequisiteCode.trim().toUpperCase();
            if (!clean.equalsIgnoreCase(this.courseCode)) {
                this.prerequisites.add(clean);
            }
        }
    }

    public void removePrerequisite(String prerequisiteCode) {
        if (prerequisiteCode != null) {
            this.prerequisites.remove(prerequisiteCode.trim().toUpperCase());
        }
    }

    public boolean hasPrerequisites() {
        return !prerequisites.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Course course = (Course) o;
        return Objects.equals(courseCode, course.courseCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(courseCode);
    }

    @Override
    public String toString() {
        return String.format("[%s] %s (%d Credits) | Dept: %s | Cap: %d | Prereqs: %s",
                courseCode, title, credits, department, maxCapacity,
                prerequisites.isEmpty() ? "None" : prerequisites.toString());
    }
}

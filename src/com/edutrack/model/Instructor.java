package com.edutrack.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a Faculty Instructor / Professor.
 * Demonstrates:
 * - Inheritance: Extends User.
 * - Encapsulation: Maintains an internal Set of assigned course codes with defensive copying.
 * - Polymorphism: Customizes details and affiliations.
 */
public class Instructor extends User {
    private static final long serialVersionUID = 1L;

    private String department;
    private String specialization;
    private final Set<String> assignedCourseCodes;

    public Instructor(String id, String name, String email, String password, String department, String specialization) {
        super(id, name, email, password, Role.INSTRUCTOR);
        this.department = (department == null || department.trim().isEmpty()) ? "Computer Science" : department.trim();
        this.specialization = (specialization == null || specialization.trim().isEmpty()) ? "General Computing" : specialization.trim();
        this.assignedCourseCodes = new HashSet<>();
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public Set<String> getAssignedCourseCodes() {
        return Collections.unmodifiableSet(assignedCourseCodes);
    }

    public void assignCourse(String courseCode) {
        if (courseCode != null && !courseCode.trim().isEmpty()) {
            assignedCourseCodes.add(courseCode.trim().toUpperCase());
        }
    }

    public void unassignCourse(String courseCode) {
        if (courseCode != null) {
            assignedCourseCodes.remove(courseCode.trim().toUpperCase());
        }
    }

    public boolean isAssignedTo(String courseCode) {
        return courseCode != null && assignedCourseCodes.contains(courseCode.trim().toUpperCase());
    }

    @Override
    public String getAffiliation() {
        return department;
    }

    @Override
    public String getRoleSpecificDetails() {
        return String.format("Department: %s | Specialization: %s | Active Courses: %d",
                department, specialization, assignedCourseCodes.size());
    }

    @Override
    public String toString() {
        return super.toString() + String.format(" | Dept: %s | Spec: %s | Courses: %s",
                department, specialization, assignedCourseCodes);
    }
}

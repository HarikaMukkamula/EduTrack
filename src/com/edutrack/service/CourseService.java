package com.edutrack.service;

import com.edutrack.data.DataStore;
import com.edutrack.exception.RecordNotFoundException;
import com.edutrack.model.Course;
import com.edutrack.model.Enrollment;
import com.edutrack.model.EnrollmentStatus;
import com.edutrack.model.Instructor;
import com.edutrack.model.User;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service managing academic courses, instructor assignments, and prerequisites.
 * Demonstrates:
 * - Domain validation and business rule enforcement.
 * - Streams for filtering, querying, and reporting.
 */
public class CourseService {
    private final DataStore dataStore;

    public CourseService(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    public Course addCourse(String code, String title, String description, int credits,
                            String department, int maxCapacity, String instructorId) {
        String cleanCode = code.trim().toUpperCase();
        if (dataStore.findCourseByCode(cleanCode).isPresent()) {
            throw new IllegalArgumentException("Course with code '" + cleanCode + "' already exists.");
        }

        // If instructor ID given, verify instructor exists
        if (instructorId != null && !instructorId.trim().isEmpty()) {
            Optional<User> ins = dataStore.findUserById(instructorId);
            if (ins.isEmpty() || !(ins.get() instanceof Instructor)) {
                throw new IllegalArgumentException("Instructor ID '" + instructorId + "' is invalid or not an instructor.");
            }
        }

        Course course = new Course(cleanCode, title, description, credits, department, maxCapacity, instructorId);
        dataStore.saveCourse(course);

        // Update instructor assignment if present
        if (instructorId != null && !instructorId.trim().isEmpty()) {
            Instructor ins = (Instructor) dataStore.findUserById(instructorId).get();
            ins.assignCourse(cleanCode);
            dataStore.saveUser(ins);
        }

        return course;
    }

    public Course getCourseByCode(String code) throws RecordNotFoundException {
        return dataStore.findCourseByCode(code)
                .orElseThrow(() -> new RecordNotFoundException("Course not found with code: " + code));
    }

    public List<Course> getAllCourses() {
        return dataStore.getAllCourses();
    }

    public List<Course> searchCourses(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllCourses();
        }
        String q = query.trim().toLowerCase();
        return dataStore.getAllCourses().stream()
                .filter(c -> c.getCourseCode().toLowerCase().contains(q)
                        || c.getTitle().toLowerCase().contains(q)
                        || c.getDepartment().toLowerCase().contains(q))
                .collect(Collectors.toList());
    }

    public List<Course> getCoursesByDepartment(String department) {
        if (department == null) return List.of();
        String dept = department.trim().toLowerCase();
        return dataStore.getAllCourses().stream()
                .filter(c -> c.getDepartment().toLowerCase().contains(dept))
                .collect(Collectors.toList());
    }

    public List<Course> getCoursesByInstructor(String instructorId) {
        if (instructorId == null) return List.of();
        String id = instructorId.trim().toUpperCase();
        return dataStore.getAllCourses().stream()
                .filter(c -> id.equalsIgnoreCase(c.getInstructorId()))
                .collect(Collectors.toList());
    }

    public void assignInstructor(String courseCode, String instructorId) throws RecordNotFoundException {
        Course course = getCourseByCode(courseCode);
        User user = dataStore.findUserById(instructorId)
                .orElseThrow(() -> new RecordNotFoundException("Instructor not found with ID: " + instructorId));

        if (!(user instanceof Instructor)) {
            throw new IllegalArgumentException("User " + instructorId + " is not an Instructor.");
        }

        // If previously assigned to another instructor, remove from old one
        if (course.getInstructorId() != null) {
            Optional<User> oldIns = dataStore.findUserById(course.getInstructorId());
            if (oldIns.isPresent() && oldIns.get() instanceof Instructor) {
                ((Instructor) oldIns.get()).unassignCourse(course.getCourseCode());
                dataStore.saveUser(oldIns.get());
            }
        }

        Instructor newInstructor = (Instructor) user;
        course.setInstructorId(newInstructor.getId());
        newInstructor.assignCourse(course.getCourseCode());

        dataStore.saveCourse(course);
        dataStore.saveUser(newInstructor);
    }

    public void addPrerequisite(String courseCode, String prerequisiteCode) throws RecordNotFoundException {
        Course course = getCourseByCode(courseCode);
        Course prereq = getCourseByCode(prerequisiteCode); // verify prerequisite exists

        if (course.getCourseCode().equalsIgnoreCase(prereq.getCourseCode())) {
            throw new IllegalArgumentException("A course cannot be a prerequisite of itself.");
        }

        course.addPrerequisite(prereq.getCourseCode());
        dataStore.saveCourse(course);
    }

    public void removePrerequisite(String courseCode, String prerequisiteCode) throws RecordNotFoundException {
        Course course = getCourseByCode(courseCode);
        course.removePrerequisite(prerequisiteCode);
        dataStore.saveCourse(course);
    }

    public int getActiveEnrollmentCount(String courseCode) {
        return (int) dataStore.getEnrollmentsByCourse(courseCode).stream()
                .filter(e -> e.getStatus() == EnrollmentStatus.ACTIVE)
                .count();
    }

    public int getAvailableSeats(String courseCode) throws RecordNotFoundException {
        Course c = getCourseByCode(courseCode);
        int active = getActiveEnrollmentCount(courseCode);
        return Math.max(0, c.getMaxCapacity() - active);
    }

    public boolean deleteCourse(String courseCode) throws RecordNotFoundException {
        Course course = getCourseByCode(courseCode);
        // Unassign from instructor if applicable
        if (course.getInstructorId() != null) {
            Optional<User> ins = dataStore.findUserById(course.getInstructorId());
            if (ins.isPresent() && ins.get() instanceof Instructor) {
                ((Instructor) ins.get()).unassignCourse(course.getCourseCode());
                dataStore.saveUser(ins.get());
            }
        }
        return dataStore.deleteCourse(courseCode);
    }
}

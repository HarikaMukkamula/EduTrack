package com.edutrack.service;

import com.edutrack.data.DataStore;
import com.edutrack.exception.*;
import com.edutrack.model.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service managing student enrollments, prerequisite verification, grading, and GPA calculation.
 * Demonstrates:
 * - Domain validation: Capacity enforcement, prerequisite rules, grade assignment.
 * - GPA calculation algorithm weighted by course credits.
 */
public class EnrollmentService {
    private final DataStore dataStore;
    private final CourseService courseService;

    public EnrollmentService(DataStore dataStore, CourseService courseService) {
        this.dataStore = dataStore;
        this.courseService = courseService;
    }

    /**
     * Enrolls a student into a course after validating capacity, duplicates, and prerequisites.
     */
    public synchronized Enrollment enrollStudent(String studentId, String courseCode, String semester)
            throws RecordNotFoundException, DuplicateEnrollmentException,
                   CourseCapacityExceededException, PrerequisiteNotMetException {

        // 1. Verify Student
        User user = dataStore.findUserById(studentId)
                .orElseThrow(() -> new RecordNotFoundException("Student with ID '" + studentId + "' not found."));
        if (!(user instanceof Student)) {
            throw new IllegalArgumentException("User " + studentId + " is not a Student.");
        }

        // 2. Verify Course
        Course course = courseService.getCourseByCode(courseCode);

        // 3. Check Duplicate Enrollment (Active or already Completed with passing grade)
        List<Enrollment> studentEnrollments = dataStore.getEnrollmentsByStudent(studentId);
        boolean alreadyActive = studentEnrollments.stream()
                .anyMatch(e -> e.getCourseCode().equalsIgnoreCase(course.getCourseCode())
                        && e.getStatus() == EnrollmentStatus.ACTIVE);
        if (alreadyActive) {
            throw new DuplicateEnrollmentException("Student is already actively enrolled in course " + course.getCourseCode());
        }

        // 4. Check Course Capacity
        int activeCount = courseService.getActiveEnrollmentCount(course.getCourseCode());
        if (activeCount >= course.getMaxCapacity()) {
            throw new CourseCapacityExceededException(
                    String.format("Course %s has reached maximum capacity (%d/%d seats filled).",
                            course.getCourseCode(), activeCount, course.getMaxCapacity()));
        }

        // 5. Check Prerequisites
        Set<String> prerequisites = course.getPrerequisites();
        for (String prereqCode : prerequisites) {
            boolean satisfied = studentEnrollments.stream()
                    .anyMatch(e -> e.getCourseCode().equalsIgnoreCase(prereqCode)
                            && e.getStatus() == EnrollmentStatus.COMPLETED
                            && e.getGrade().isPassing());
            if (!satisfied) {
                Course prereqCourse = dataStore.findCourseByCode(prereqCode).orElse(null);
                String prereqTitle = (prereqCourse != null) ? prereqCourse.getTitle() : prereqCode;
                throw new PrerequisiteNotMetException(
                        String.format("Prerequisite not met: Course %s (%s) must be successfully completed before taking %s.",
                                prereqCode, prereqTitle, course.getCourseCode()));
            }
        }

        // 6. Generate Enrollment and Persist
        String enrollmentId = generateNextEnrollmentId();
        Enrollment enrollment = new Enrollment(enrollmentId, studentId, course.getCourseCode(), semester);
        dataStore.saveEnrollment(enrollment);
        return enrollment;
    }

    /**
     * Drops an active course enrollment.
     */
    public boolean dropCourse(String enrollmentId, String studentId) throws RecordNotFoundException {
        Enrollment enrollment = dataStore.findEnrollmentById(enrollmentId)
                .orElseThrow(() -> new RecordNotFoundException("Enrollment not found with ID: " + enrollmentId));

        if (!enrollment.getStudentId().equalsIgnoreCase(studentId.trim())) {
            throw new IllegalArgumentException("This enrollment does not belong to student " + studentId);
        }

        if (enrollment.getStatus() == EnrollmentStatus.DROPPED) {
            return false;
        }

        enrollment.drop();
        dataStore.saveEnrollment(enrollment);
        return true;
    }

    /**
     * Instructors or Admins submit grades and scores for an enrollment.
     */
    public void submitGrade(String enrollmentId, double score, String graderId)
            throws RecordNotFoundException, AuthenticationException {

        Enrollment enrollment = dataStore.findEnrollmentById(enrollmentId)
                .orElseThrow(() -> new RecordNotFoundException("Enrollment not found with ID: " + enrollmentId));

        // Grader verification
        if (graderId != null) {
            User grader = dataStore.findUserById(graderId)
                    .orElseThrow(() -> new RecordNotFoundException("Grader ID '" + graderId + "' not found."));

            if (grader instanceof Instructor) {
                Course course = courseService.getCourseByCode(enrollment.getCourseCode());
                if (course.getInstructorId() == null || !course.getInstructorId().equalsIgnoreCase(graderId)) {
                    throw new AuthenticationException("Instructor " + graderId + " is not assigned to course " + course.getCourseCode());
                }
            } else if (!(grader instanceof Admin)) {
                throw new AuthenticationException("User " + graderId + " does not have grading permissions.");
            }
        }

        enrollment.recordScore(score);
        dataStore.saveEnrollment(enrollment);
    }

    public List<Enrollment> getStudentEnrollments(String studentId) {
        return dataStore.getEnrollmentsByStudent(studentId);
    }

    public List<Enrollment> getCourseEnrollments(String courseCode) {
        return dataStore.getEnrollmentsByCourse(courseCode);
    }

    public List<Enrollment> getActiveEnrollmentsForCourse(String courseCode) {
        return dataStore.getEnrollmentsByCourse(courseCode).stream()
                .filter(e -> e.getStatus() == EnrollmentStatus.ACTIVE)
                .collect(Collectors.toList());
    }

    /**
     * Calculates cumulative Grade Point Average (GPA) on a 4.0 scale weighted by credit hours.
     */
    public double calculateGPA(String studentId) {
        List<Enrollment> completedList = dataStore.getEnrollmentsByStudent(studentId).stream()
                .filter(e -> e.getStatus() == EnrollmentStatus.COMPLETED && e.getGrade() != Grade.IN_PROGRESS)
                .collect(Collectors.toList());

        if (completedList.isEmpty()) {
            return 0.0;
        }

        double totalQualityPoints = 0.0;
        int totalCredits = 0;

        for (Enrollment e : completedList) {
            Optional<Course> courseOpt = dataStore.findCourseByCode(e.getCourseCode());
            int credits = courseOpt.map(Course::getCredits).orElse(3);
            double points = e.getGrade().getGradePoint() * credits;

            totalQualityPoints += points;
            totalCredits += credits;
        }

        return (totalCredits > 0) ? (totalQualityPoints / totalCredits) : 0.0;
    }

    private synchronized String generateNextEnrollmentId() {
        int max = 1000;
        for (Enrollment e : dataStore.getAllEnrollments()) {
            String id = e.getEnrollmentId();
            if (id.startsWith("ENR-")) {
                try {
                    int num = Integer.parseInt(id.substring(4));
                    if (num > max) max = num;
                } catch (NumberFormatException ignored) {}
            }
        }
        return "ENR-" + (max + 1);
    }
}

package com.edutrack.data;

import com.edutrack.model.Course;
import com.edutrack.model.Enrollment;
import com.edutrack.model.Role;
import com.edutrack.model.User;

import java.util.List;
import java.util.Optional;

/**
 * Interface defining the repository contract for EduTrack data storage.
 * Demonstrates:
 * - Abstraction: Separates high-level business operations from underlying storage mechanism
 *   (in-memory Collections or file persistence).
 */
public interface DataStore {
    // User Operations
    void saveUser(User user);
    Optional<User> findUserById(String id);
    Optional<User> findUserByEmail(String email);
    List<User> getAllUsers();
    List<User> getUsersByRole(Role role);
    boolean deleteUser(String id);

    // Course Operations
    void saveCourse(Course course);
    Optional<Course> findCourseByCode(String code);
    List<Course> getAllCourses();
    boolean deleteCourse(String code);

    // Enrollment Operations
    void saveEnrollment(Enrollment enrollment);
    Optional<Enrollment> findEnrollmentById(String id);
    List<Enrollment> getAllEnrollments();
    List<Enrollment> getEnrollmentsByStudent(String studentId);
    List<Enrollment> getEnrollmentsByCourse(String courseCode);
    boolean deleteEnrollment(String enrollmentId);

    // Persistence Operations
    void saveToStorage();
    void loadFromStorage();
}

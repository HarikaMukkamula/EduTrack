package com.edutrack.service;

import com.edutrack.data.DataStore;
import com.edutrack.exception.RecordNotFoundException;
import com.edutrack.model.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service responsible for generating university-wide reports, academic transcripts,
 * and course performance analytics.
 */
public class ReportService {
    private final DataStore dataStore;
    private final CourseService courseService;
    private final EnrollmentService enrollmentService;

    public ReportService(DataStore dataStore, CourseService courseService, EnrollmentService enrollmentService) {
        this.dataStore = dataStore;
        this.courseService = courseService;
        this.enrollmentService = enrollmentService;
    }

    public static class SystemStats {
        public int totalUsers;
        public int totalAdmins;
        public int totalInstructors;
        public int totalStudents;
        public int totalCourses;
        public int totalEnrollments;
        public int activeEnrollments;
        public int completedEnrollments;
    }

    public SystemStats getSystemStats() {
        SystemStats stats = new SystemStats();
        List<User> users = dataStore.getAllUsers();
        stats.totalUsers = users.size();
        stats.totalAdmins = (int) users.stream().filter(u -> u.getRole() == Role.ADMIN).count();
        stats.totalInstructors = (int) users.stream().filter(u -> u.getRole() == Role.INSTRUCTOR).count();
        stats.totalStudents = (int) users.stream().filter(u -> u.getRole() == Role.STUDENT).count();

        stats.totalCourses = dataStore.getAllCourses().size();

        List<Enrollment> enrollments = dataStore.getAllEnrollments();
        stats.totalEnrollments = enrollments.size();
        stats.activeEnrollments = (int) enrollments.stream().filter(e -> e.getStatus() == EnrollmentStatus.ACTIVE).count();
        stats.completedEnrollments = (int) enrollments.stream().filter(e -> e.getStatus() == EnrollmentStatus.COMPLETED).count();

        return stats;
    }

    public static class CoursePerformance {
        public String courseCode;
        public String courseTitle;
        public String instructorName;
        public int totalEnrolled;
        public int completedCount;
        public int activeCount;
        public double averageScore;
        public double passRate;
        public Map<Grade, Integer> gradeDistribution = new TreeMap<>();
    }

    public CoursePerformance getCoursePerformance(String courseCode) throws RecordNotFoundException {
        Course course = courseService.getCourseByCode(courseCode);
        CoursePerformance perf = new CoursePerformance();
        perf.courseCode = course.getCourseCode();
        perf.courseTitle = course.getTitle();

        if (course.getInstructorId() != null) {
            perf.instructorName = dataStore.findUserById(course.getInstructorId())
                    .map(User::getName)
                    .orElse("Unassigned");
        } else {
            perf.instructorName = "Unassigned";
        }

        List<Enrollment> enrollments = dataStore.getEnrollmentsByCourse(courseCode);
        perf.totalEnrolled = enrollments.size();

        List<Enrollment> completed = enrollments.stream()
                .filter(e -> e.getStatus() == EnrollmentStatus.COMPLETED && e.getScore() >= 0)
                .collect(Collectors.toList());

        perf.completedCount = completed.size();
        perf.activeCount = (int) enrollments.stream().filter(e -> e.getStatus() == EnrollmentStatus.ACTIVE).count();

        if (!completed.isEmpty()) {
            double sum = completed.stream().mapToDouble(Enrollment::getScore).sum();
            perf.averageScore = sum / completed.size();

            long passed = completed.stream().filter(e -> e.getGrade().isPassing()).count();
            perf.passRate = (passed * 100.0) / completed.size();

            for (Enrollment e : completed) {
                perf.gradeDistribution.put(e.getGrade(), perf.gradeDistribution.getOrDefault(e.getGrade(), 0) + 1);
            }
        } else {
            perf.averageScore = 0.0;
            perf.passRate = 0.0;
        }

        return perf;
    }
}

package com.edutrack.data;

import com.edutrack.model.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Concrete implementation of DataStore combining in-memory Java Collections
 * (Maps, Lists, Sets) with transparent file persistence via FileStorageManager.
 * Demonstrates:
 * - Pure OOP Abstraction: Implements DataStore interface.
 * - Collection Framework: Uses ConcurrentHashMap, ArrayList, Set, and Stream API.
 * - Defensive Copying: Returns unmodifiable or newly created lists to preserve encapsulation.
 */
public class InMemoryDataStore implements DataStore {
    private final Map<String, User> userMap;
    private final Map<String, Course> courseMap;
    private final Map<String, Enrollment> enrollmentMap;
    private final FileStorageManager fileStorageManager;

    public InMemoryDataStore(String dataDirectory) {
        this.userMap = new ConcurrentHashMap<>();
        this.courseMap = new ConcurrentHashMap<>();
        this.enrollmentMap = new ConcurrentHashMap<>();
        this.fileStorageManager = new FileStorageManager(dataDirectory);

        // Load existing records or seed initial university data
        init();
    }

    private void init() {
        if (fileStorageManager.dataFilesExist()) {
            loadFromStorage();
        } else {
            seedDefaultData();
            saveToStorage();
        }
    }

    // ==================== USER OPERATIONS ====================

    @Override
    public void saveUser(User user) {
        if (user != null) {
            userMap.put(user.getId().toUpperCase(), user);
            saveToStorage();
        }
    }

    @Override
    public Optional<User> findUserById(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(userMap.get(id.trim().toUpperCase()));
    }

    @Override
    public Optional<User> findUserByEmail(String email) {
        if (email == null) return Optional.empty();
        String target = email.trim().toLowerCase();
        return userMap.values().stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(target))
                .findFirst();
    }

    @Override
    public List<User> getAllUsers() {
        return new ArrayList<>(userMap.values());
    }

    @Override
    public List<User> getUsersByRole(Role role) {
        if (role == null) return Collections.emptyList();
        return userMap.values().stream()
                .filter(u -> u.getRole() == role)
                .sorted(Comparator.comparing(User::getId))
                .collect(Collectors.toList());
    }

    @Override
    public boolean deleteUser(String id) {
        if (id == null) return false;
        User removed = userMap.remove(id.trim().toUpperCase());
        if (removed != null) {
            saveToStorage();
            return true;
        }
        return false;
    }

    // ==================== COURSE OPERATIONS ====================

    @Override
    public void saveCourse(Course course) {
        if (course != null) {
            courseMap.put(course.getCourseCode().toUpperCase(), course);
            saveToStorage();
        }
    }

    @Override
    public Optional<Course> findCourseByCode(String code) {
        if (code == null) return Optional.empty();
        String clean = code.trim().toUpperCase();
        Course c = courseMap.get(clean);
        if (c != null) return Optional.of(c);

        if (clean.startsWith("CS") && !clean.startsWith("CSE")) {
            c = courseMap.get(clean.replaceFirst("CS", "CSE"));
            if (c != null) return Optional.of(c);
        } else if (clean.startsWith("CSE")) {
            c = courseMap.get(clean.replaceFirst("CSE", "CS"));
            if (c != null) return Optional.of(c);
        }
        return Optional.empty();
    }

    @Override
    public List<Course> getAllCourses() {
        return courseMap.values().stream()
                .sorted(Comparator.comparing(Course::getCourseCode))
                .collect(Collectors.toList());
    }

    @Override
    public boolean deleteCourse(String code) {
        if (code == null) return false;
        Course removed = courseMap.remove(code.trim().toUpperCase());
        if (removed != null) {
            saveToStorage();
            return true;
        }
        return false;
    }

    // ==================== ENROLLMENT OPERATIONS ====================

    @Override
    public void saveEnrollment(Enrollment enrollment) {
        if (enrollment != null) {
            enrollmentMap.put(enrollment.getEnrollmentId().toUpperCase(), enrollment);
            saveToStorage();
        }
    }

    @Override
    public Optional<Enrollment> findEnrollmentById(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(enrollmentMap.get(id.trim().toUpperCase()));
    }

    @Override
    public List<Enrollment> getAllEnrollments() {
        return new ArrayList<>(enrollmentMap.values());
    }

    @Override
    public List<Enrollment> getEnrollmentsByStudent(String studentId) {
        if (studentId == null) return Collections.emptyList();
        String target = studentId.trim().toUpperCase();
        return enrollmentMap.values().stream()
                .filter(e -> e.getStudentId().equalsIgnoreCase(target))
                .sorted(Comparator.comparing(Enrollment::getCourseCode))
                .collect(Collectors.toList());
    }

    @Override
    public List<Enrollment> getEnrollmentsByCourse(String courseCode) {
        if (courseCode == null) return Collections.emptyList();
        String target = courseCode.trim().toUpperCase();
        return enrollmentMap.values().stream()
                .filter(e -> e.getCourseCode().equalsIgnoreCase(target))
                .sorted(Comparator.comparing(Enrollment::getStudentId))
                .collect(Collectors.toList());
    }

    @Override
    public boolean deleteEnrollment(String enrollmentId) {
        if (enrollmentId == null) return false;
        Enrollment removed = enrollmentMap.remove(enrollmentId.trim().toUpperCase());
        if (removed != null) {
            saveToStorage();
            return true;
        }
        return false;
    }

    // ==================== STORAGE LIFECYCLE ====================

    @Override
    public synchronized void saveToStorage() {
        try {
            fileStorageManager.saveUsers(userMap.values());
            fileStorageManager.saveCourses(courseMap.values());
            fileStorageManager.saveEnrollments(enrollmentMap.values());
        } catch (IOException e) {
            System.err.println("Error saving to files: " + e.getMessage());
        }
    }

    @Override
    public synchronized void loadFromStorage() {
        try {
            userMap.clear();
            courseMap.clear();
            enrollmentMap.clear();

            for (User u : fileStorageManager.loadUsers()) {
                userMap.put(u.getId().toUpperCase(), u);
            }
            for (Course c : fileStorageManager.loadCourses()) {
                courseMap.put(c.getCourseCode().toUpperCase(), c);
            }
            for (Enrollment e : fileStorageManager.loadEnrollments()) {
                enrollmentMap.put(e.getEnrollmentId().toUpperCase(), e);
            }
        } catch (IOException e) {
            System.err.println("Error loading from files: " + e.getMessage());
        }
    }

    // ==================== SEED MOCK DATA ====================

    private void seedDefaultData() {
        // 1. Administrators
        Admin admin1 = new Admin("ADM101", "Dr. Sarah Jenkins", "admin@gcet.edu.in", "admin123", "Academic Affairs", "University Registrar");
        Admin admin2 = new Admin("ADM102", "Mark Henderson", "mark@gcet.edu.in", "admin123", "Faculty of Engineering", "Department Dean");
        userMap.put(admin1.getId().toUpperCase(), admin1);
        userMap.put(admin2.getId().toUpperCase(), admin2);

        // 2. Instructors
        Instructor ins1 = new Instructor("INS201", "Prof. Alan Turing", "turing@gcet.edu.in", "prof123", "CSE", "Algorithms & Theory");
        Instructor ins2 = new Instructor("INS202", "Prof. Grace Hopper", "hopper@gcet.edu.in", "prof123", "CSE", "Programming Systems & Compilers");
        Instructor ins3 = new Instructor("INS203", "Dr. Claude Shannon", "shannon@gcet.edu.in", "prof123", "Electrical & Computer Eng", "Networks & Information Theory");

        ins1.assignCourse("CSE102");
        ins1.assignCourse("CSE301");
        ins2.assignCourse("CSE101");
        ins2.assignCourse("CSE201");
        ins3.assignCourse("EE205");

        userMap.put(ins1.getId().toUpperCase(), ins1);
        userMap.put(ins2.getId().toUpperCase(), ins2);
        userMap.put(ins3.getId().toUpperCase(), ins3);

        // 3. Students - Both Harika and Rahul are in 2nd year CSE
        Student stu1 = new Student("STU301", "Harika Mukkamula", "harika@gcet.edu.in", "student123", "CSE", 2, 3, "9876543210");
        Student stu2 = new Student("STU302", "Rahul Sharma", "rahul@gcet.edu.in", "student123", "CSE", 2, 3, "9876543211");
        Student stu3 = new Student("STU303", "Priya Patel", "priya@gcet.edu.in", "student123", "CSE", 2, 3, "9876543212");
        Student stu4 = new Student("STU304", "David Miller", "david@gcet.edu.in", "student123", "Data Science", 1, 1, "9876543213");

        userMap.put(stu1.getId().toUpperCase(), stu1);
        userMap.put(stu2.getId().toUpperCase(), stu2);
        userMap.put(stu3.getId().toUpperCase(), stu3);
        userMap.put(stu4.getId().toUpperCase(), stu4);

        // 4. Courses
        Course c1 = new Course("CSE101", "Introduction to Java OOP", "Core principles of OOP, encapsulation, polymorphism, and Java fundamentals", 4, "CSE", 45, "INS202");
        Course c2 = new Course("CSE102", "Data Structures & Algorithms", "Analysis and implementation of lists, trees, graphs, hashing, and sorting", 4, "CSE", 40, "INS201");
        c2.addPrerequisite("CSE101");

        Course c3 = new Course("CSE201", "Software Design Patterns", "GoF design patterns, UML, refactoring, and clean architecture", 3, "CSE", 35, "INS202");
        c3.addPrerequisite("CSE101");

        Course c4 = new Course("CSE301", "Database Management Systems", "Relational algebra, SQL, normalization, transaction processing, and indexing", 4, "CSE", 40, "INS201");
        c4.addPrerequisite("CSE102");

        Course c5 = new Course("EE205", "Computer Networks & Protocols", "Layered network architecture, TCP/IP, routing protocols, and socket programming", 3, "Electrical & Computer Eng", 30, "INS203");
        c5.addPrerequisite("CSE101");

        Course c6 = new Course("CSE405", "Cloud Computing & Distributed Systems", "Microservices, virtualization, distributed storage, and AWS/GCP architecture", 3, "CSE", 30, "INS201");
        c6.addPrerequisite("CSE301");

        courseMap.put(c1.getCourseCode(), c1);
        courseMap.put(c2.getCourseCode(), c2);
        courseMap.put(c3.getCourseCode(), c3);
        courseMap.put(c4.getCourseCode(), c4);
        courseMap.put(c5.getCourseCode(), c5);
        courseMap.put(c6.getCourseCode(), c6);

        // 5. Enrollments
        Enrollment e1 = new Enrollment("ENR-1001", "STU301", "CSE101", "Spring 2026", 93.5, Grade.A, EnrollmentStatus.COMPLETED, "2026-01-15 10:00");
        Enrollment e2 = new Enrollment("ENR-1002", "STU301", "CSE102", "Fall 2026", 86.0, Grade.B, EnrollmentStatus.COMPLETED, "2026-07-20 11:30");
        Enrollment e3 = new Enrollment("ENR-1003", "STU301", "CSE201", "Fall 2026", -1.0, Grade.IN_PROGRESS, EnrollmentStatus.ACTIVE, "2026-08-01 09:15");

        Enrollment e4 = new Enrollment("ENR-1004", "STU302", "CSE101", "Spring 2026", 78.0, Grade.C, EnrollmentStatus.COMPLETED, "2026-01-16 14:00");
        Enrollment e5 = new Enrollment("ENR-1005", "STU302", "CSE102", "Fall 2026", -1.0, Grade.IN_PROGRESS, EnrollmentStatus.ACTIVE, "2026-08-02 10:00");

        Enrollment e6 = new Enrollment("ENR-1006", "STU303", "CSE101", "Spring 2026", 96.0, Grade.A, EnrollmentStatus.COMPLETED, "2026-01-15 10:30");
        Enrollment e7 = new Enrollment("ENR-1007", "STU303", "CSE102", "Fall 2026", 91.0, Grade.A, EnrollmentStatus.COMPLETED, "2026-07-21 14:30");
        Enrollment e8 = new Enrollment("ENR-1008", "STU303", "CSE301", "Fall 2026", -1.0, Grade.IN_PROGRESS, EnrollmentStatus.ACTIVE, "2026-08-05 16:00");

        enrollmentMap.put(e1.getEnrollmentId(), e1);
        enrollmentMap.put(e2.getEnrollmentId(), e2);
        enrollmentMap.put(e3.getEnrollmentId(), e3);
        enrollmentMap.put(e4.getEnrollmentId(), e4);
        enrollmentMap.put(e5.getEnrollmentId(), e5);
        enrollmentMap.put(e6.getEnrollmentId(), e6);
        enrollmentMap.put(e7.getEnrollmentId(), e7);
        enrollmentMap.put(e8.getEnrollmentId(), e8);
    }
}

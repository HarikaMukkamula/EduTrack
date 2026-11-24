package com.edutrack;

import com.edutrack.data.DataStore;
import com.edutrack.data.DataStoreFactory;
import com.edutrack.data.InMemoryDataStore;
import com.edutrack.exception.AuthenticationException;
import com.edutrack.exception.CourseCapacityExceededException;
import com.edutrack.exception.DuplicateEnrollmentException;
import com.edutrack.exception.PrerequisiteNotMetException;
import com.edutrack.exception.RecordNotFoundException;
import com.edutrack.model.*;
import com.edutrack.service.*;
import com.edutrack.util.ConsoleUtils;
import com.edutrack.util.ValidationUtils;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Main Entry Point for EduTrack: Object-Oriented Course Management System.
 * Demonstrates:
 * - Pure Object-Oriented Architecture (Encapsulation, Inheritance, Polymorphism, Abstraction).
 * - Layered design: Model, Data Persistence, Service layer, Presentation layer.
 * - Robust interactive console menus with validation and error handling.
 */
public class EduTrackApp {
    private final UserService userService;
    private final CourseService courseService;
    private final EnrollmentService enrollmentService;
    private final ReportService reportService;
    private final DataStore dataStore;

    private User currentUser = null;

    public EduTrackApp(String dataDir) {
        this.dataStore = DataStoreFactory.createDataStore(dataDir);
        this.userService = new UserService(dataStore);
        this.courseService = new CourseService(dataStore);
        this.enrollmentService = new EnrollmentService(dataStore, courseService);
        this.reportService = new ReportService(dataStore, courseService, enrollmentService);
    }

    public static void main(String[] args) {
        String dataDir = "data";
        if (args.length > 0) {
            dataDir = args[0];
        }
        EduTrackApp app = new EduTrackApp(dataDir);
        app.run();
    }

    public void run() {
        boolean running = true;
        while (running) {
            if (currentUser == null) {
                running = displayPublicMenu();
            } else {
                switch (currentUser.getRole()) {
                    case ADMIN:
                        displayAdminMenu();
                        break;
                    case INSTRUCTOR:
                        displayInstructorMenu();
                        break;
                    case STUDENT:
                        displayStudentMenu();
                        break;
                }
            }
        }
        ConsoleUtils.printHeader("THANK YOU FOR USING EDUTRACK");
        System.out.println("All data changes have been securely persisted to disk.");
        System.out.println("Goodbye!\n");
    }

    // ==================== PUBLIC / MAIN MENU ====================

    private boolean displayPublicMenu() {
        ConsoleUtils.printHeader("EDUTRACK: UNIVERSITY COURSE MANAGEMENT SYSTEM");
        System.out.println("  Database: " + (DataStoreFactory.isUsingMySql() ? "[MySQL edutrack_db - LIVE 🟢]" : "[CSV File Storage]"));
        System.out.println();
        System.out.println("  1. User Login (Admin / Instructor / Student)");
        System.out.println("  2. Student Self-Registration");
        System.out.println("  3. Browse Public Course Catalog");
        System.out.println("  4. Quick Demo Accounts & System Info");
        System.out.println("  5. Exit System");
        System.out.println();

        int choice = ConsoleUtils.readInt("Select an option", 1, 5);
        switch (choice) {
            case 1:
                handleLogin();
                break;
            case 2:
                handleStudentRegistration();
                break;
            case 3:
                handleBrowsePublicCatalog();
                break;
            case 4:
                handleDisplayDemoAccounts();
                break;
            case 5:
                return false;
        }
        return true;
    }

    private void handleLogin() {
        ConsoleUtils.printSection("USER AUTHENTICATION");
        System.out.println("Enter your credentials (User ID or registered Email):");
        String identifier = ConsoleUtils.readString("User ID or Email", false);
        String password = ConsoleUtils.readString("Password", false);

        try {
            currentUser = userService.login(identifier, password);
            ConsoleUtils.printSuccess("Welcome back, " + currentUser.getName() + " [" + currentUser.getRole().getDisplayName() + "]!");
            ConsoleUtils.printInfo("Affiliation: " + currentUser.getAffiliation());
            ConsoleUtils.pressEnterToContinue();
        } catch (AuthenticationException e) {
            ConsoleUtils.printError(e.getMessage());
            ConsoleUtils.pressEnterToContinue();
        }
    }

    private void handleStudentRegistration() {
        ConsoleUtils.printSection("STUDENT SELF-REGISTRATION");
        String name = ConsoleUtils.readString("Full Legal Name", false);
        
        String email;
        while (true) {
            email = ConsoleUtils.readString("University Email", false);
            if (ValidationUtils.isValidEmail(email)) break;
            ConsoleUtils.printWarning("Please enter a valid email format (e.g., student@gcet.edu.in).");
        }

        String password = ConsoleUtils.readString("Create Password", false);
        String major = ConsoleUtils.readString("Academic Major / Degree (e.g. CSE)", false);
        int year = ConsoleUtils.readInt("Year of Study (1-4)", 1, 4);
        int sem = ConsoleUtils.readInt("Current Semester (1-8)", 1, 8);
        String phone = ConsoleUtils.readString("Contact Phone Number", false);

        try {
            Student student = userService.registerStudent(name, email, password, major, year, sem, phone);
            ConsoleUtils.printSuccess("Registration Successful! Your assigned Student ID is: " + student.getId());
            ConsoleUtils.printInfo("You can now log in using ID '" + student.getId() + "' or email '" + student.getEmail() + "'.");
        } catch (Exception e) {
            ConsoleUtils.printError("Registration Failed: " + e.getMessage());
        }
        ConsoleUtils.pressEnterToContinue();
    }

    private void handleBrowsePublicCatalog() {
        ConsoleUtils.printSection("UNIVERSITY COURSE CATALOG");
        List<Course> courses = courseService.getAllCourses();
        displayCourseTable(courses);
        ConsoleUtils.pressEnterToContinue();
    }

    private void handleDisplayDemoAccounts() {
        ConsoleUtils.printSection("PRE-LOADED DEMO ACCOUNTS & CREDENTIALS");
        String[] headers = {"Role", "User ID", "Email", "Password", "Designation / Major"};
        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"ADMIN", "ADM101", "admin@gcet.edu.in", "admin123", "Dr. Sarah Jenkins (Registrar)"});
        rows.add(new String[]{"ADMIN", "ADM102", "mark@gcet.edu.in", "admin123", "Mark Henderson (Dept Head)"});
        rows.add(new String[]{"INSTRUCTOR", "INS201", "turing@gcet.edu.in", "prof123", "Prof. Alan Turing (CSE)"});
        rows.add(new String[]{"INSTRUCTOR", "INS202", "hopper@gcet.edu.in", "prof123", "Prof. Grace Hopper (CSE)"});
        rows.add(new String[]{"STUDENT", "STU301", "harika@gcet.edu.in", "student123", "Harika Mukkamula (CSE, Yr 2)"});
        rows.add(new String[]{"STUDENT", "STU302", "rahul@gcet.edu.in", "student123", "Rahul Sharma (CSE, Yr 2)"});
        ConsoleUtils.printTable(headers, rows);
        ConsoleUtils.printInfo("These accounts come preloaded with courses, active enrollments, and academic transcripts.");
        ConsoleUtils.pressEnterToContinue();
    }

    // ==================== ADMIN MENU ====================

    private void displayAdminMenu() {
        Admin admin = (Admin) currentUser;
        ConsoleUtils.printHeader("ADMIN DASHBOARD - " + admin.getName().toUpperCase());
        System.out.println("Dept: " + admin.getDepartment() + " | Level: " + admin.getAdminLevel());
        System.out.println();
        System.out.println("  [Course Management]");
        System.out.println("   1. Add New Course");
        System.out.println("   2. View All Courses Catalog");
        System.out.println("   3. Assign Instructor to Course");
        System.out.println("   4. Add Course Prerequisite");
        System.out.println("   5. Delete Course");
        System.out.println();
        System.out.println("  [User Management]");
        System.out.println("   6. View All Students");
        System.out.println("   7. View All Instructors");
        System.out.println("   8. Register New Instructor");
        System.out.println("   9. Register New Admin");
        System.out.println("  10. Delete a User Account");
        System.out.println();
        System.out.println("  [University Analytics & Reports]");
        System.out.println("  11. View University System Summary");
        System.out.println("  12. View Course Performance & Grade Distribution");
        System.out.println("  13. View Student Academic Transcript");
        System.out.println();
        System.out.println("   0. Logout");
        System.out.println();

        int choice = ConsoleUtils.readInt("Select an option", 0, 13);
        switch (choice) {
            case 1: adminAddCourse(); break;
            case 2: adminViewCourses(); break;
            case 3: adminAssignInstructor(); break;
            case 4: adminAddPrerequisite(); break;
            case 5: adminDeleteCourse(); break;
            case 6: adminViewStudents(); break;
            case 7: adminViewInstructors(); break;
            case 8: adminRegisterInstructor(); break;
            case 9: adminRegisterAdmin(); break;
            case 10: adminDeleteUser(); break;
            case 11: adminViewSystemStats(); break;
            case 12: viewCoursePerformance(); break;
            case 13: viewStudentTranscriptAdmin(); break;
            case 0:
                currentUser = null;
                ConsoleUtils.printSuccess("Logged out successfully.");
                break;
        }
    }

    private void adminAddCourse() {
        ConsoleUtils.printSection("CREATE NEW COURSE");
        String code = ConsoleUtils.readString("Course Code (e.g., CSE401)", false).toUpperCase();
        String title = ConsoleUtils.readString("Course Title", false);
        String desc = ConsoleUtils.readString("Description / Syllabus Overview", false);
        int credits = ConsoleUtils.readInt("Credit Hours (1-6)", 1, 6);
        String dept = ConsoleUtils.readString("Department (e.g. CSE)", false);
        int cap = ConsoleUtils.readInt("Maximum Student Capacity", 5, 200);
        String insId = ConsoleUtils.readString("Assigned Instructor ID (optional, press Enter to skip)", true);

        try {
            Course c = courseService.addCourse(code, title, desc, credits, dept, cap, insId.isEmpty() ? null : insId);
            ConsoleUtils.printSuccess("Course '" + c.getCourseCode() + " - " + c.getTitle() + "' successfully created!");
        } catch (Exception e) {
            ConsoleUtils.printError("Failed to add course: " + e.getMessage());
        }
        ConsoleUtils.pressEnterToContinue();
    }

    private void adminViewCourses() {
        ConsoleUtils.printSection("ALL REGISTERED COURSES");
        displayCourseTable(courseService.getAllCourses());
        ConsoleUtils.pressEnterToContinue();
    }

    private void adminAssignInstructor() {
        ConsoleUtils.printSection("ASSIGN INSTRUCTOR TO COURSE");
        String courseCode = ConsoleUtils.readString("Course Code", false).toUpperCase();
        String instructorId = ConsoleUtils.readString("Instructor ID (e.g. INS201)", false).toUpperCase();

        try {
            courseService.assignInstructor(courseCode, instructorId);
            ConsoleUtils.printSuccess("Successfully assigned " + instructorId + " to course " + courseCode);
        } catch (Exception e) {
            ConsoleUtils.printError(e.getMessage());
        }
        ConsoleUtils.pressEnterToContinue();
    }

    private void adminAddPrerequisite() {
        ConsoleUtils.printSection("ADD COURSE PREREQUISITE");
        String targetCourse = ConsoleUtils.readString("Target Course Code (e.g., CSE102)", false).toUpperCase();
        String prereqCourse = ConsoleUtils.readString("Prerequisite Course Code (e.g., CSE101)", false).toUpperCase();

        try {
            courseService.addPrerequisite(targetCourse, prereqCourse);
            ConsoleUtils.printSuccess("Prerequisite '" + prereqCourse + "' added to '" + targetCourse + "'!");
        } catch (Exception e) {
            ConsoleUtils.printError(e.getMessage());
        }
        ConsoleUtils.pressEnterToContinue();
    }

    private void adminDeleteCourse() {
        ConsoleUtils.printSection("DELETE COURSE");
        String code = ConsoleUtils.readString("Enter Course Code to Delete", false).toUpperCase();
        boolean confirm = ConsoleUtils.readYesNo("Are you sure you want to delete course " + code + "?");
        if (confirm) {
            try {
                courseService.deleteCourse(code);
                ConsoleUtils.printSuccess("Course " + code + " deleted successfully.");
            } catch (Exception e) {
                ConsoleUtils.printError(e.getMessage());
            }
        }
        ConsoleUtils.pressEnterToContinue();
    }

    private void adminViewStudents() {
        ConsoleUtils.printSection("ENROLLED STUDENTS DIRECTORY");
        List<User> students = userService.getUsersByRole(Role.STUDENT);
        String[] headers = {"Student ID", "Name", "Email", "Major", "Year", "Sem", "GPA"};
        List<String[]> rows = new ArrayList<>();
        for (User u : students) {
            Student s = (Student) u;
            double gpa = enrollmentService.calculateGPA(s.getId());
            rows.add(new String[]{
                    s.getId(), s.getName(), s.getEmail(), s.getMajor(),
                    String.valueOf(s.getYearOfStudy()), String.valueOf(s.getCurrentSemester()),
                    String.format("%.2f", gpa)
            });
        }
        ConsoleUtils.printTable(headers, rows);
        ConsoleUtils.pressEnterToContinue();
    }

    private void adminViewInstructors() {
        ConsoleUtils.printSection("FACULTY INSTRUCTORS DIRECTORY");
        List<User> instructors = userService.getUsersByRole(Role.INSTRUCTOR);
        String[] headers = {"Instructor ID", "Name", "Email", "Department", "Specialization", "Assigned Courses"};
        List<String[]> rows = new ArrayList<>();
        for (User u : instructors) {
            Instructor ins = (Instructor) u;
            rows.add(new String[]{
                    ins.getId(), ins.getName(), ins.getEmail(), ins.getDepartment(),
                    ins.getSpecialization(), String.join(", ", ins.getAssignedCourseCodes())
            });
        }
        ConsoleUtils.printTable(headers, rows);
        ConsoleUtils.pressEnterToContinue();
    }

    private void adminRegisterInstructor() {
        ConsoleUtils.printSection("REGISTER FACULTY INSTRUCTOR");
        String name = ConsoleUtils.readString("Instructor Full Name", false);
        String email = ConsoleUtils.readString("Instructor Email", false);
        String password = ConsoleUtils.readString("Temporary Password", false);
        String dept = ConsoleUtils.readString("Department", false);
        String spec = ConsoleUtils.readString("Specialization / Research Area", false);

        try {
            Instructor ins = userService.registerInstructor(name, email, password, dept, spec);
            ConsoleUtils.printSuccess("Instructor registered! Assigned ID: " + ins.getId());
        } catch (Exception e) {
            ConsoleUtils.printError("Registration failed: " + e.getMessage());
        }
        ConsoleUtils.pressEnterToContinue();
    }

    private void adminRegisterAdmin() {
        ConsoleUtils.printSection("REGISTER NEW ADMINISTRATOR");
        String name = ConsoleUtils.readString("Admin Full Name", false);
        String email = ConsoleUtils.readString("Admin Email", false);
        String password = ConsoleUtils.readString("Password", false);
        String dept = ConsoleUtils.readString("Administrative Department", false);
        String level = ConsoleUtils.readString("Role / Designation (e.g., Dean, Registrar)", false);

        try {
            Admin adm = userService.registerAdmin(name, email, password, dept, level);
            ConsoleUtils.printSuccess("Admin registered! Assigned ID: " + adm.getId());
        } catch (Exception e) {
            ConsoleUtils.printError("Registration failed: " + e.getMessage());
        }
        ConsoleUtils.pressEnterToContinue();
    }

    private void adminDeleteUser() {
        ConsoleUtils.printSection("DELETE USER ACCOUNT");
        String id = ConsoleUtils.readString("Enter User ID to delete (e.g. STU302)", false).toUpperCase();
        if (id.equalsIgnoreCase(currentUser.getId())) {
            ConsoleUtils.printError("You cannot delete your own active administrator account.");
            ConsoleUtils.pressEnterToContinue();
            return;
        }

        try {
            User target = userService.getUserById(id);
            boolean confirm = ConsoleUtils.readYesNo("Delete " + target.getRole() + " " + target.getName() + " (" + target.getId() + ")?");
            if (confirm) {
                userService.removeUser(id);
                ConsoleUtils.printSuccess("User " + id + " has been removed.");
            }
        } catch (Exception e) {
            ConsoleUtils.printError(e.getMessage());
        }
        ConsoleUtils.pressEnterToContinue();
    }

    private void adminViewSystemStats() {
        ConsoleUtils.printSection("UNIVERSITY SYSTEM SUMMARY");
        ReportService.SystemStats stats = reportService.getSystemStats();
        String[] headers = {"Metric", "Count"};
        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"Total Registered Users", String.valueOf(stats.totalUsers)});
        rows.add(new String[]{"  • Administrators", String.valueOf(stats.totalAdmins)});
        rows.add(new String[]{"  • Faculty Instructors", String.valueOf(stats.totalInstructors)});
        rows.add(new String[]{"  • Enrolled Students", String.valueOf(stats.totalStudents)});
        rows.add(new String[]{"Total University Courses", String.valueOf(stats.totalCourses)});
        rows.add(new String[]{"Total Course Enrollments", String.valueOf(stats.totalEnrollments)});
        rows.add(new String[]{"  • Active Course Seats", String.valueOf(stats.activeEnrollments)});
        rows.add(new String[]{"  • Completed Course Credits", String.valueOf(stats.completedEnrollments)});
        ConsoleUtils.printTable(headers, rows);
        ConsoleUtils.pressEnterToContinue();
    }

    private void viewCoursePerformance() {
        ConsoleUtils.printSection("COURSE PERFORMANCE & GRADE ANALYTICS");
        String courseCode = ConsoleUtils.readString("Enter Course Code (e.g. CSE101)", false).toUpperCase();

        try {
            ReportService.CoursePerformance perf = reportService.getCoursePerformance(courseCode);
            System.out.println("Course: " + perf.courseCode + " - " + perf.courseTitle);
            System.out.println("Instructor: " + perf.instructorName);
            System.out.println("Total Enrollments: " + perf.totalEnrolled + " (Active: " + perf.activeCount + ", Completed: " + perf.completedCount + ")");
            System.out.println(String.format("Average Score: %.2f / 100.0 | Pass Rate: %.1f%%", perf.averageScore, perf.passRate));

            if (!perf.gradeDistribution.isEmpty()) {
                System.out.println("\nGrade Distribution:");
                perf.gradeDistribution.forEach((grade, count) -> {
                    String bar = "█".repeat(count * 2);
                    System.out.println(String.format("  Grade %-2s (%3.1f pts): %2d student(s) %s", grade.getLetter(), grade.getGradePoint(), count, bar));
                });
            } else {
                ConsoleUtils.printInfo("No graded records available yet for this course.");
            }
        } catch (Exception e) {
            ConsoleUtils.printError(e.getMessage());
        }
        ConsoleUtils.pressEnterToContinue();
    }

    private void viewStudentTranscriptAdmin() {
        ConsoleUtils.printSection("STUDENT ACADEMIC TRANSCRIPT LOOKUP");
        String studentId = ConsoleUtils.readString("Enter Student ID (e.g., STU301)", false).toUpperCase();
        try {
            User user = userService.getUserById(studentId);
            if (!(user instanceof Student)) {
                ConsoleUtils.printError("User " + studentId + " is not a Student.");
            } else {
                printStudentTranscript((Student) user);
            }
        } catch (Exception e) {
            ConsoleUtils.printError(e.getMessage());
        }
        ConsoleUtils.pressEnterToContinue();
    }

    // ==================== INSTRUCTOR MENU ====================

    private void displayInstructorMenu() {
        Instructor instructor = (Instructor) currentUser;
        ConsoleUtils.printHeader("INSTRUCTOR PORTAL - " + instructor.getName().toUpperCase());
        System.out.println(instructor.getRoleSpecificDetails());
        System.out.println();
        System.out.println("  1. View My Assigned Courses");
        System.out.println("  2. View Course Class Roster (Enrolled Students)");
        System.out.println("  3. Enter / Update Student Grades & Marks");
        System.out.println("  4. View Course Performance Analytics");
        System.out.println("  5. View Faculty Profile");
        System.out.println("  0. Logout");
        System.out.println();

        int choice = ConsoleUtils.readInt("Select an option", 0, 5);
        switch (choice) {
            case 1: instructorViewAssignedCourses(); break;
            case 2: instructorViewRoster(); break;
            case 3: instructorSubmitGrades(); break;
            case 4: viewCoursePerformance(); break;
            case 5:
                ConsoleUtils.printSection("FACULTY PROFILE");
                System.out.println("ID:             " + instructor.getId());
                System.out.println("Name:           " + instructor.getName());
                System.out.println("Email:          " + instructor.getEmail());
                System.out.println("Department:     " + instructor.getDepartment());
                System.out.println("Specialization: " + instructor.getSpecialization());
                System.out.println("Courses:        " + String.join(", ", instructor.getAssignedCourseCodes()));
                ConsoleUtils.pressEnterToContinue();
                break;
            case 0:
                currentUser = null;
                ConsoleUtils.printSuccess("Logged out successfully.");
                break;
        }
    }

    private void instructorViewAssignedCourses() {
        Instructor instructor = (Instructor) currentUser;
        ConsoleUtils.printSection("ASSIGNED TEACHING COURSES");
        List<Course> myCourses = courseService.getCoursesByInstructor(instructor.getId());
        displayCourseTable(myCourses);
        ConsoleUtils.pressEnterToContinue();
    }

    private void instructorViewRoster() {
        Instructor instructor = (Instructor) currentUser;
        ConsoleUtils.printSection("COURSE CLASS ROSTER");
        String courseCode = ConsoleUtils.readString("Enter Course Code (e.g., CSE101)", false).toUpperCase();

        if (!instructor.isAssignedTo(courseCode)) {
            ConsoleUtils.printWarning("Note: You are not the primary assigned instructor for this course.");
        }

        List<Enrollment> enrollments = enrollmentService.getCourseEnrollments(courseCode);
        String[] headers = {"Enrollment ID", "Student ID", "Student Name", "Semester", "Status", "Score", "Grade"};
        List<String[]> rows = new ArrayList<>();

        for (Enrollment e : enrollments) {
            String studentName = dataStore.findUserById(e.getStudentId()).map(User::getName).orElse("Unknown");
            String scoreStr = e.getScore() >= 0 ? String.format("%.1f", e.getScore()) : "Pending";
            rows.add(new String[]{
                    e.getEnrollmentId(), e.getStudentId(), studentName,
                    e.getSemester(), e.getStatus().name(), scoreStr, e.getGrade().getLetter()
            });
        }
        ConsoleUtils.printTable(headers, rows);
        ConsoleUtils.pressEnterToContinue();
    }

    private void instructorSubmitGrades() {
        Instructor instructor = (Instructor) currentUser;
        ConsoleUtils.printSection("SUBMIT / UPDATE STUDENT GRADES");
        String enrId = ConsoleUtils.readString("Enrollment ID (e.g. ENR-1003)", false).toUpperCase();

        try {
            Enrollment enr = dataStore.findEnrollmentById(enrId)
                    .orElseThrow(() -> new RecordNotFoundException("Enrollment not found with ID: " + enrId));

            String studentName = dataStore.findUserById(enr.getStudentId()).map(User::getName).orElse("Unknown");
            System.out.println("Student: " + studentName + " (" + enr.getStudentId() + ")");
            System.out.println("Course:  " + enr.getCourseCode());
            System.out.println("Current Grade: " + enr.getGrade().getLetter() + " (Score: " + (enr.getScore() >= 0 ? enr.getScore() : "None") + ")");

            double score = ConsoleUtils.readDouble("Enter Score / Marks (0.0 - 100.0)", 0.0, 100.0);
            enrollmentService.submitGrade(enrId, score, instructor.getId());

            Enrollment updated = dataStore.findEnrollmentById(enrId).get();
            ConsoleUtils.printSuccess(String.format("Grade recorded: %.1f -> %s (%s)",
                    updated.getScore(), updated.getGrade().getLetter(), updated.getGrade().getDescription()));
        } catch (Exception e) {
            ConsoleUtils.printError(e.getMessage());
        }
        ConsoleUtils.pressEnterToContinue();
    }

    // ==================== STUDENT MENU ====================

    private void displayStudentMenu() {
        Student student = (Student) currentUser;
        ConsoleUtils.printHeader("STUDENT PORTAL - " + student.getName().toUpperCase());
        double gpa = enrollmentService.calculateGPA(student.getId());
        System.out.println(String.format("ID: %s | Major: %s | Year %d (Sem %d) | Cumulative GPA: %.2f / 4.0",
                student.getId(), student.getMajor(), student.getYearOfStudy(), student.getCurrentSemester(), gpa));
        System.out.println();
        System.out.println("  1. Browse Available Courses & Prerequisites");
        System.out.println("  2. Register / Enroll in a Course");
        System.out.println("  3. Drop a Course Enrollment");
        System.out.println("  4. View My Current Enrolled Courses");
        System.out.println("  5. View Academic Transcript & GPA");
        System.out.println("  6. View Student Profile");
        System.out.println("  0. Logout");
        System.out.println();

        int choice = ConsoleUtils.readInt("Select an option", 0, 6);
        switch (choice) {
            case 1:
                ConsoleUtils.printSection("AVAILABLE COURSE CATALOG");
                displayCourseTable(courseService.getAllCourses());
                ConsoleUtils.pressEnterToContinue();
                break;
            case 2:
                studentEnrollCourse();
                break;
            case 3:
                studentDropCourse();
                break;
            case 4:
                studentViewCurrentCourses();
                break;
            case 5:
                ConsoleUtils.printSection("MY OFFICIAL ACADEMIC TRANSCRIPT");
                printStudentTranscript(student);
                ConsoleUtils.pressEnterToContinue();
                break;
            case 6:
                ConsoleUtils.printSection("STUDENT PROFILE DETAILS");
                System.out.println("Student ID:       " + student.getId());
                System.out.println("Full Name:        " + student.getName());
                System.out.println("Email:            " + student.getEmail());
                System.out.println("Academic Major:   " + student.getMajor());
                System.out.println("Year of Study:    Year " + student.getYearOfStudy());
                System.out.println("Current Semester: Semester " + student.getCurrentSemester());
                System.out.println("Contact Phone:    " + student.getContactNumber());
                System.out.println(String.format("Cumulative GPA:   %.2f / 4.0", gpa));
                ConsoleUtils.pressEnterToContinue();
                break;
            case 0:
                currentUser = null;
                ConsoleUtils.printSuccess("Logged out successfully.");
                break;
        }
    }

    private void studentEnrollCourse() {
        Student student = (Student) currentUser;
        ConsoleUtils.printSection("ENROLL IN A COURSE");
        String courseCode = ConsoleUtils.readString("Enter Course Code to Enroll (e.g. CSE102)", false).toUpperCase();
        String semester = ConsoleUtils.readString("Semester (e.g., Fall 2026)", false);

        try {
            Enrollment enrollment = enrollmentService.enrollStudent(student.getId(), courseCode, semester);
            ConsoleUtils.printSuccess("Congratulations! Successfully enrolled in course " + courseCode);
            ConsoleUtils.printInfo("Enrollment Reference: " + enrollment.getEnrollmentId() + " (" + enrollment.getSemester() + ")");
        } catch (DuplicateEnrollmentException | CourseCapacityExceededException | PrerequisiteNotMetException e) {
            ConsoleUtils.printWarning("Registration Unsuccessful: " + e.getMessage());
        } catch (Exception e) {
            ConsoleUtils.printError("Error: " + e.getMessage());
        }
        ConsoleUtils.pressEnterToContinue();
    }

    private void studentDropCourse() {
        Student student = (Student) currentUser;
        ConsoleUtils.printSection("DROP A COURSE");
        List<Enrollment> activeList = enrollmentService.getStudentEnrollments(student.getId()).stream()
                .filter(e -> e.getStatus() == EnrollmentStatus.ACTIVE)
                .collect(Collectors.toList());

        if (activeList.isEmpty()) {
            ConsoleUtils.printInfo("You have no active course enrollments to drop.");
            ConsoleUtils.pressEnterToContinue();
            return;
        }

        String[] headers = {"Enrollment ID", "Course Code", "Course Title", "Semester"};
        List<String[]> rows = new ArrayList<>();
        for (Enrollment e : activeList) {
            String title = dataStore.findCourseByCode(e.getCourseCode()).map(Course::getTitle).orElse("");
            rows.add(new String[]{e.getEnrollmentId(), e.getCourseCode(), title, e.getSemester()});
        }
        ConsoleUtils.printTable(headers, rows);

        String enrId = ConsoleUtils.readString("Enter Enrollment ID to drop (or 'c' to cancel)", false).toUpperCase();
        if (enrId.equals("C")) return;

        try {
            boolean confirm = ConsoleUtils.readYesNo("Are you sure you want to drop enrollment " + enrId + "?");
            if (confirm) {
                enrollmentService.dropCourse(enrId, student.getId());
                ConsoleUtils.printSuccess("Enrollment " + enrId + " has been successfully dropped.");
            }
        } catch (Exception e) {
            ConsoleUtils.printError(e.getMessage());
        }
        ConsoleUtils.pressEnterToContinue();
    }

    private void studentViewCurrentCourses() {
        Student student = (Student) currentUser;
        ConsoleUtils.printSection("MY ACTIVE ENROLLMENTS & TIMETABLE");
        List<Enrollment> list = enrollmentService.getStudentEnrollments(student.getId()).stream()
                .filter(e -> e.getStatus() == EnrollmentStatus.ACTIVE)
                .collect(Collectors.toList());

        if (list.isEmpty()) {
            ConsoleUtils.printInfo("No active course enrollments at this moment.");
        } else {
            String[] headers = {"Enrollment ID", "Course Code", "Course Title", "Credits", "Instructor", "Semester"};
            List<String[]> rows = new ArrayList<>();
            for (Enrollment e : list) {
                Course c = dataStore.findCourseByCode(e.getCourseCode()).orElse(null);
                String title = (c != null) ? c.getTitle() : "N/A";
                String credits = (c != null) ? String.valueOf(c.getCredits()) : "-";
                String instructor = "Unassigned";
                if (c != null && c.getInstructorId() != null) {
                    instructor = dataStore.findUserById(c.getInstructorId()).map(User::getName).orElse("Unassigned");
                }
                rows.add(new String[]{e.getEnrollmentId(), e.getCourseCode(), title, credits, instructor, e.getSemester()});
            }
            ConsoleUtils.printTable(headers, rows);
        }
        ConsoleUtils.pressEnterToContinue();
    }

    private void printStudentTranscript(Student student) {
        List<Enrollment> allEnrollments = enrollmentService.getStudentEnrollments(student.getId());

        System.out.println("Student Name:   " + student.getName());
        System.out.println("Student ID:     " + student.getId());
        System.out.println("Program/Major:  " + student.getMajor());
        System.out.println("Academic Year:  Year " + student.getYearOfStudy() + " (Sem " + student.getCurrentSemester() + ")");
        System.out.println();

        String[] headers = {"Course Code", "Course Title", "Credits", "Semester", "Status", "Score", "Grade", "Points"};
        List<String[]> rows = new ArrayList<>();

        int totalEarnedCredits = 0;

        for (Enrollment e : allEnrollments) {
            Course c = dataStore.findCourseByCode(e.getCourseCode()).orElse(null);
            String title = (c != null) ? c.getTitle() : "N/A";
            int credits = (c != null) ? c.getCredits() : 3;

            String scoreStr = e.getScore() >= 0 ? String.format("%.1f", e.getScore()) : "Pending";
            String pointsStr = e.isGraded() ? String.format("%.1f", e.getGrade().getGradePoint()) : "-";

            if (e.getStatus() == EnrollmentStatus.COMPLETED && e.getGrade().isPassing()) {
                totalEarnedCredits += credits;
            }

            rows.add(new String[]{
                    e.getCourseCode(), title, String.valueOf(credits),
                    e.getSemester(), e.getStatus().name(), scoreStr,
                    e.getGrade().getLetter(), pointsStr
            });
        }

        ConsoleUtils.printTable(headers, rows);

        double gpa = enrollmentService.calculateGPA(student.getId());
        System.out.println();
        System.out.println("─────────────────────────────────────────────────────────────");
        System.out.println(String.format("Total Academic Credits Earned: %d Credits", totalEarnedCredits));
        System.out.println(String.format("Cumulative Grade Point Average (GPA): %.2f / 4.00", gpa));
        System.out.println("Academic Standing: " + (gpa >= 3.5 ? "Dean's Honor List" : (gpa >= 2.0 ? "Good Academic Standing" : "Academic Probation Warning")));
        System.out.println("─────────────────────────────────────────────────────────────");
    }

    // ==================== SHARED HELPERS ====================

    private void displayCourseTable(List<Course> courses) {
        String[] headers = {"Code", "Title", "Credits", "Department", "Instructor", "Seats (Filled/Max)", "Prerequisites"};
        List<String[]> rows = new ArrayList<>();

        for (Course c : courses) {
            String instructorName = "Unassigned";
            if (c.getInstructorId() != null) {
                instructorName = dataStore.findUserById(c.getInstructorId())
                        .map(User::getName)
                        .orElse("ID: " + c.getInstructorId());
            }

            int activeCount = courseService.getActiveEnrollmentCount(c.getCourseCode());
            String seats = activeCount + " / " + c.getMaxCapacity();
            String prereqs = c.getPrerequisites().isEmpty() ? "None" : String.join(", ", c.getPrerequisites());

            rows.add(new String[]{
                    c.getCourseCode(), c.getTitle(), String.valueOf(c.getCredits()),
                    c.getDepartment(), instructorName, seats, prereqs
            });
        }

        ConsoleUtils.printTable(headers, rows);
    }
}

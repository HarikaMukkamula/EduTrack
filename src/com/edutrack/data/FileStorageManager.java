package com.edutrack.data;

import com.edutrack.model.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Handles persistent storage via simple, clean CSV file handling.
 * Demonstrates:
 * - Robust Java I/O (BufferedReader, BufferedWriter, Path, Files).
 * - Exception resilience: Gracefully recovers and seeds default records if files do not exist.
 */
public class FileStorageManager {
    private final String dataDirectory;
    private final Path usersFilePath;
    private final Path coursesFilePath;
    private final Path enrollmentsFilePath;

    public FileStorageManager(String dataDirectory) {
        this.dataDirectory = dataDirectory;
        this.usersFilePath = Paths.get(dataDirectory, "users.csv");
        this.coursesFilePath = Paths.get(dataDirectory, "courses.csv");
        this.enrollmentsFilePath = Paths.get(dataDirectory, "enrollments.csv");
        ensureDirectoryExists();
    }

    private void ensureDirectoryExists() {
        try {
            Path dir = Paths.get(dataDirectory);
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }
        } catch (IOException e) {
            System.err.println("Warning: Could not create data directory: " + e.getMessage());
        }
    }

    public boolean dataFilesExist() {
        return Files.exists(usersFilePath) && Files.exists(coursesFilePath) && Files.exists(enrollmentsFilePath);
    }

    // ==================== SAVE OPERATIONS ====================

    public void saveUsers(Collection<User> users) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(usersFilePath, StandardCharsets.UTF_8)) {
            writer.write("#TYPE,ID,NAME,EMAIL,PASSWORD,EXTRA1,EXTRA2,EXTRA3,EXTRA4");
            writer.newLine();

            for (User user : users) {
                if (user instanceof Admin) {
                    Admin a = (Admin) user;
                    writer.write(String.format("ADMIN,%s,%s,%s,%s,%s,%s,,",
                            escape(a.getId()), escape(a.getName()), escape(a.getEmail()),
                            escape(a.getPassword()), escape(a.getDepartment()), escape(a.getAdminLevel())));
                } else if (user instanceof Instructor) {
                    Instructor ins = (Instructor) user;
                    String courseList = String.join(";", ins.getAssignedCourseCodes());
                    writer.write(String.format("INSTRUCTOR,%s,%s,%s,%s,%s,%s,%s,",
                            escape(ins.getId()), escape(ins.getName()), escape(ins.getEmail()),
                            escape(ins.getPassword()), escape(ins.getDepartment()),
                            escape(ins.getSpecialization()), escape(courseList)));
                } else if (user instanceof Student) {
                    Student s = (Student) user;
                    writer.write(String.format("STUDENT,%s,%s,%s,%s,%s,%d,%d,%s",
                            escape(s.getId()), escape(s.getName()), escape(s.getEmail()),
                            escape(s.getPassword()), escape(s.getMajor()), s.getYearOfStudy(),
                            s.getCurrentSemester(), escape(s.getContactNumber())));
                }
                writer.newLine();
            }
        }
    }

    public void saveCourses(Collection<Course> courses) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(coursesFilePath, StandardCharsets.UTF_8)) {
            writer.write("#COURSE_CODE,TITLE,DESCRIPTION,CREDITS,DEPARTMENT,MAX_CAPACITY,INSTRUCTOR_ID,PREREQUISITES");
            writer.newLine();

            for (Course c : courses) {
                String prereqs = String.join(";", c.getPrerequisites());
                writer.write(String.format("%s,%s,%s,%d,%s,%d,%s,%s",
                        escape(c.getCourseCode()), escape(c.getTitle()), escape(c.getDescription()),
                        c.getCredits(), escape(c.getDepartment()), c.getMaxCapacity(),
                        escape(c.getInstructorId() != null ? c.getInstructorId() : ""),
                        escape(prereqs)));
                writer.newLine();
            }
        }
    }

    public void saveEnrollments(Collection<Enrollment> enrollments) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(enrollmentsFilePath, StandardCharsets.UTF_8)) {
            writer.write("#ENROLLMENT_ID,STUDENT_ID,COURSE_CODE,SEMESTER,SCORE,GRADE,STATUS,ENROLLMENT_DATE");
            writer.newLine();

            for (Enrollment e : enrollments) {
                writer.write(String.format("%s,%s,%s,%s,%.2f,%s,%s,%s",
                        escape(e.getEnrollmentId()), escape(e.getStudentId()), escape(e.getCourseCode()),
                        escape(e.getSemester()), e.getScore(), e.getGrade().name(),
                        e.getStatus().name(), escape(e.getEnrollmentDate())));
                writer.newLine();
            }
        }
    }

    // ==================== LOAD OPERATIONS ====================

    public List<User> loadUsers() throws IOException {
        List<User> list = new ArrayList<>();
        if (!Files.exists(usersFilePath)) return list;

        try (BufferedReader reader = Files.newBufferedReader(usersFilePath, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                String[] parts = line.split(",", -1);
                if (parts.length < 5) continue;

                String type = parts[0].trim().toUpperCase();
                String id = unescape(parts[1]);
                String name = unescape(parts[2]);
                String email = unescape(parts[3]);
                String password = unescape(parts[4]);

                switch (type) {
                    case "ADMIN":
                        String dept = parts.length > 5 ? unescape(parts[5]) : "Administration";
                        String level = parts.length > 6 ? unescape(parts[6]) : "Registrar";
                        list.add(new Admin(id, name, email, password, dept, level));
                        break;
                    case "INSTRUCTOR":
                        String insDept = parts.length > 5 ? unescape(parts[5]) : "Computer Science";
                        String spec = parts.length > 6 ? unescape(parts[6]) : "General";
                        Instructor instructor = new Instructor(id, name, email, password, insDept, spec);
                        if (parts.length > 7 && !parts[7].trim().isEmpty()) {
                            String[] codes = unescape(parts[7]).split(";");
                            for (String c : codes) {
                                instructor.assignCourse(c);
                            }
                        }
                        list.add(instructor);
                        break;
                    case "STUDENT":
                        String major = parts.length > 5 ? unescape(parts[5]) : "Computer Science";
                        int year = 1;
                        int sem = 1;
                        try {
                            if (parts.length > 6 && !parts[6].trim().isEmpty()) year = Integer.parseInt(parts[6].trim());
                            if (parts.length > 7 && !parts[7].trim().isEmpty()) sem = Integer.parseInt(parts[7].trim());
                        } catch (NumberFormatException ignored) {}
                        String phone = parts.length > 8 ? unescape(parts[8]) : "N/A";
                        list.add(new Student(id, name, email, password, major, year, sem, phone));
                        break;
                }
            }
        }
        return list;
    }

    public List<Course> loadCourses() throws IOException {
        List<Course> list = new ArrayList<>();
        if (!Files.exists(coursesFilePath)) return list;

        try (BufferedReader reader = Files.newBufferedReader(coursesFilePath, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                String[] parts = line.split(",", -1);
                if (parts.length < 6) continue;

                String code = unescape(parts[0]);
                String title = unescape(parts[1]);
                String desc = unescape(parts[2]);
                int credits = Integer.parseInt(parts[3].trim());
                String dept = unescape(parts[4]);
                int cap = Integer.parseInt(parts[5].trim());
                String instructorId = (parts.length > 6 && !parts[6].trim().isEmpty()) ? unescape(parts[6]) : null;

                Course course = new Course(code, title, desc, credits, dept, cap, instructorId);
                if (parts.length > 7 && !parts[7].trim().isEmpty()) {
                    String[] prereqs = unescape(parts[7]).split(";");
                    for (String p : prereqs) {
                        course.addPrerequisite(p);
                    }
                }
                list.add(course);
            }
        }
        return list;
    }

    public List<Enrollment> loadEnrollments() throws IOException {
        List<Enrollment> list = new ArrayList<>();
        if (!Files.exists(enrollmentsFilePath)) return list;

        try (BufferedReader reader = Files.newBufferedReader(enrollmentsFilePath, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                String[] parts = line.split(",", -1);
                if (parts.length < 8) continue;

                String enrId = unescape(parts[0]);
                String studentId = unescape(parts[1]);
                String courseCode = unescape(parts[2]);
                String semester = unescape(parts[3]);
                double score = Double.parseDouble(parts[4].trim());
                Grade grade = Grade.fromString(parts[5].trim());
                EnrollmentStatus status = EnrollmentStatus.fromString(parts[6].trim());
                String date = unescape(parts[7]);

                Enrollment enr = new Enrollment(enrId, studentId, courseCode, semester, score, grade, status, date);
                list.add(enr);
            }
        }
        return list;
    }

    // ==================== CSV HELPERS ====================

    private String escape(String val) {
        if (val == null) return "";
        return val.replace("\n", " ").replace(",", ";;");
    }

    private String unescape(String val) {
        if (val == null) return "";
        return val.replace(";;", ",");
    }
}

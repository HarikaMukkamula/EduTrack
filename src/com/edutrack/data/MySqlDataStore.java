package com.edutrack.data;

import com.edutrack.model.*;

import java.sql.*;
import java.util.*;

/**
 * MySqlDataStore: Production relational database implementation of DataStore.
 * Executes live SQL queries against MySQL database (edutrack_db) via JDBC.
 */
public class MySqlDataStore implements DataStore {

    public MySqlDataStore() {
        initDatabase();
    }

    private void initDatabase() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            // Create users table
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "id VARCHAR(20) PRIMARY KEY, " +
                    "name VARCHAR(100) NOT NULL, " +
                    "email VARCHAR(100) UNIQUE NOT NULL, " +
                    "password VARCHAR(100) NOT NULL, " +
                    "role VARCHAR(20) NOT NULL, " +
                    "department VARCHAR(100), " +
                    "admin_level VARCHAR(50), " +
                    "specialization VARCHAR(100), " +
                    "major VARCHAR(100), " +
                    "year_of_study INT DEFAULT 1, " +
                    "current_semester INT DEFAULT 1, " +
                    "contact_number VARCHAR(20), " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            // Create courses table
            stmt.execute("CREATE TABLE IF NOT EXISTS courses (" +
                    "course_code VARCHAR(20) PRIMARY KEY, " +
                    "title VARCHAR(150) NOT NULL, " +
                    "description TEXT, " +
                    "credits INT NOT NULL, " +
                    "department VARCHAR(100) NOT NULL, " +
                    "max_capacity INT NOT NULL DEFAULT 40, " +
                    "instructor_id VARCHAR(20), " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            // Create prerequisites table
            stmt.execute("CREATE TABLE IF NOT EXISTS course_prerequisites (" +
                    "course_code VARCHAR(20) NOT NULL, " +
                    "prerequisite_code VARCHAR(20) NOT NULL, " +
                    "PRIMARY KEY (course_code, prerequisite_code))");

            // Create instructor courses table
            stmt.execute("CREATE TABLE IF NOT EXISTS instructor_courses (" +
                    "instructor_id VARCHAR(20) NOT NULL, " +
                    "course_code VARCHAR(20) NOT NULL, " +
                    "PRIMARY KEY (instructor_id, course_code))");

            // Create enrollments table
            stmt.execute("CREATE TABLE IF NOT EXISTS enrollments (" +
                    "enrollment_id VARCHAR(20) PRIMARY KEY, " +
                    "student_id VARCHAR(20) NOT NULL, " +
                    "course_code VARCHAR(20) NOT NULL, " +
                    "semester VARCHAR(30) NOT NULL, " +
                    "score DECIMAL(5,2) DEFAULT -1.00, " +
                    "grade VARCHAR(15) DEFAULT 'IN_PROGRESS', " +
                    "status VARCHAR(20) DEFAULT 'ACTIVE', " +
                    "enrollment_date VARCHAR(30), " +
                    "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP)");

            // Check if seeding is needed
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    seedInitialData(conn);
                }
            }
        } catch (SQLException e) {
            System.err.println("[DATABASE ERROR] Failed to initialize MySQL schema: " + e.getMessage());
        }
    }

    private void seedInitialData(Connection conn) throws SQLException {
        // Run seed queries if database was fresh
        String seedUsers = "INSERT IGNORE INTO users (id, name, email, password, role, department, admin_level, specialization, major, year_of_study, current_semester, contact_number) VALUES " +
                "('ADM101', 'Dr. Sarah Jenkins', 'admin@gcet.edu.in', 'admin123', 'ADMIN', 'Academic Affairs', 'University Registrar', NULL, NULL, 1, 1, NULL)," +
                "('ADM102', 'Mark Henderson', 'mark@gcet.edu.in', 'admin123', 'ADMIN', 'Faculty of Engineering', 'Department Dean', NULL, NULL, 1, 1, NULL)," +
                "('INS201', 'Prof. Alan Turing', 'turing@gcet.edu.in', 'prof123', 'INSTRUCTOR', 'CSE', NULL, 'Algorithms & Theory', NULL, 1, 1, NULL)," +
                "('INS202', 'Prof. Grace Hopper', 'hopper@gcet.edu.in', 'prof123', 'INSTRUCTOR', 'CSE', NULL, 'Programming Systems & Compilers', NULL, 1, 1, NULL)," +
                "('INS203', 'Dr. Claude Shannon', 'shannon@gcet.edu.in', 'prof123', 'INSTRUCTOR', 'Electrical & Computer Eng', NULL, 'Networks & Information Theory', NULL, 1, 1, NULL)," +
                "('STU301', 'Harika Mukkamula', 'harika@gcet.edu.in', 'student123', 'STUDENT', NULL, NULL, NULL, 'CSE', 2, 3, '9876543210')," +
                "('STU302', 'Rahul Sharma', 'rahul@gcet.edu.in', 'student123', 'STUDENT', NULL, NULL, NULL, 'CSE', 2, 3, '9876543211')," +
                "('STU303', 'Priya Patel', 'priya@gcet.edu.in', 'student123', 'STUDENT', NULL, NULL, NULL, 'CSE', 2, 3, '9876543212')," +
                "('STU304', 'David Miller', 'david@gcet.edu.in', 'student123', 'STUDENT', NULL, NULL, NULL, 'Data Science', 1, 1, '9876543213')," +
                "('STU305', 'TUPAKULA SOWMYA', '25r11a05lp@gcet.edu.in', '25R11A05LP', 'STUDENT', NULL, NULL, NULL, 'CSE', 2, 3, '6302038180')";

        String seedCourses = "INSERT IGNORE INTO courses (course_code, title, description, credits, department, max_capacity, instructor_id) VALUES " +
                "('CSE101', 'Introduction to Java OOP', 'Core principles of OOP, encapsulation, polymorphism, and Java fundamentals', 4, 'CSE', 45, 'INS202')," +
                "('CSE102', 'Data Structures & Algorithms', 'Analysis and implementation of lists, trees, graphs, hashing, and sorting', 4, 'CSE', 40, 'INS201')," +
                "('CSE201', 'Software Design Patterns', 'GoF design patterns, UML, refactoring, and clean architecture', 3, 'CSE', 35, 'INS202')," +
                "('CSE301', 'Database Management Systems', 'Relational algebra, SQL, normalization, transaction processing, and indexing', 4, 'CSE', 40, 'INS201')," +
                "('CSE405', 'Cloud Computing & Distributed Systems', 'Microservices, virtualization, distributed storage, and AWS/GCP architecture', 3, 'CSE', 30, 'INS201')," +
                "('EE205', 'Computer Networks & Protocols', 'Layered network architecture, TCP/IP, routing protocols, and socket programming', 3, 'Electrical & Computer Eng', 30, 'INS203')";

        String seedPrereqs = "INSERT IGNORE INTO course_prerequisites (course_code, prerequisite_code) VALUES " +
                "('CSE102', 'CSE101'), ('CSE201', 'CSE101'), ('CSE301', 'CSE102'), ('CSE405', 'CSE301'), ('EE205', 'CSE101')";

        String seedInsCourses = "INSERT IGNORE INTO instructor_courses (instructor_id, course_code) VALUES " +
                "('INS201', 'CSE102'), ('INS201', 'CSE301'), ('INS201', 'CSE405'), ('INS202', 'CSE101'), ('INS202', 'CSE201'), ('INS203', 'EE205')";

        String seedEnrollments = "INSERT IGNORE INTO enrollments (enrollment_id, student_id, course_code, semester, score, grade, status, enrollment_date) VALUES " +
                "('ENR-1001', 'STU301', 'CSE101', 'Spring 2026', 93.50, 'A', 'COMPLETED', '2026-01-15 10:00')," +
                "('ENR-1002', 'STU301', 'CSE102', 'Fall 2026', 86.00, 'B', 'COMPLETED', '2026-07-20 11:30')," +
                "('ENR-1003', 'STU301', 'CSE201', 'Fall 2026', -1.00, 'IN_PROGRESS', 'ACTIVE', '2026-08-01 09:15')," +
                "('ENR-1009', 'STU301', 'CSE301', 'Fall 2026', 94.00, 'A', 'COMPLETED', '2026-10-16 19:38')," +
                "('ENR-1004', 'STU302', 'CSE101', 'Spring 2026', 78.00, 'C', 'COMPLETED', '2026-01-16 14:00')," +
                "('ENR-1005', 'STU302', 'CSE102', 'Fall 2026', -1.00, 'IN_PROGRESS', 'ACTIVE', '2026-08-02 10:00')," +
                "('ENR-1006', 'STU303', 'CSE101', 'Spring 2026', 96.00, 'A', 'COMPLETED', '2026-01-15 10:30')," +
                "('ENR-1007', 'STU303', 'CSE102', 'Fall 2026', 91.00, 'A', 'COMPLETED', '2026-07-21 14:30')," +
                "('ENR-1008', 'STU303', 'CSE301', 'Fall 2026', -1.00, 'IN_PROGRESS', 'ACTIVE', '2026-08-05 16:00')";

        try (Statement s = conn.createStatement()) {
            s.execute(seedUsers);
            s.execute(seedCourses);
            s.execute(seedPrereqs);
            s.execute(seedInsCourses);
            s.execute(seedEnrollments);
        }
    }

    // ==========================================
    // USER OPERATIONS
    // ==========================================

    @Override
    public void saveUser(User user) {
        if (user == null) return;
        String sql = "INSERT INTO users (id, name, email, password, role, department, admin_level, specialization, major, year_of_study, current_semester, contact_number) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE name=VALUES(name), email=VALUES(email), password=VALUES(password), " +
                "department=VALUES(department), admin_level=VALUES(admin_level), specialization=VALUES(specialization), " +
                "major=VALUES(major), year_of_study=VALUES(year_of_study), current_semester=VALUES(current_semester), contact_number=VALUES(contact_number)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, user.getId().toUpperCase());
            ps.setString(2, user.getName());
            ps.setString(3, user.getEmail().toLowerCase());
            ps.setString(4, user.getPassword());
            ps.setString(5, user.getRole().name());

            if (user instanceof Admin) {
                Admin a = (Admin) user;
                ps.setString(6, a.getDepartment());
                ps.setString(7, a.getAdminLevel());
                ps.setNull(8, Types.VARCHAR);
                ps.setNull(9, Types.VARCHAR);
                ps.setInt(10, 1);
                ps.setInt(11, 1);
                ps.setNull(12, Types.VARCHAR);
            } else if (user instanceof Instructor) {
                Instructor ins = (Instructor) user;
                ps.setString(6, ins.getDepartment());
                ps.setNull(7, Types.VARCHAR);
                ps.setString(8, ins.getSpecialization());
                ps.setNull(9, Types.VARCHAR);
                ps.setInt(10, 1);
                ps.setInt(11, 1);
                ps.setNull(12, Types.VARCHAR);
            } else if (user instanceof Student) {
                Student s = (Student) user;
                ps.setNull(6, Types.VARCHAR);
                ps.setNull(7, Types.VARCHAR);
                ps.setNull(8, Types.VARCHAR);
                ps.setString(9, s.getMajor());
                ps.setInt(10, s.getYearOfStudy());
                ps.setInt(11, s.getCurrentSemester());
                ps.setString(12, s.getContactNumber());
            }

            ps.executeUpdate();

            // Sync instructor course assignments
            if (user instanceof Instructor) {
                Instructor ins = (Instructor) user;
                try (PreparedStatement del = conn.prepareStatement("DELETE FROM instructor_courses WHERE instructor_id = ?")) {
                    del.setString(1, ins.getId());
                    del.executeUpdate();
                }
                if (!ins.getAssignedCourseCodes().isEmpty()) {
                    try (PreparedStatement insStmt = conn.prepareStatement("INSERT IGNORE INTO instructor_courses (instructor_id, course_code) VALUES (?, ?)")) {
                        for (String code : ins.getAssignedCourseCodes()) {
                            insStmt.setString(1, ins.getId());
                            insStmt.setString(2, code);
                            insStmt.addBatch();
                        }
                        insStmt.executeBatch();
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[DATABASE ERROR] saveUser: " + e.getMessage());
        }
    }

    @Override
    public Optional<User> findUserById(String id) {
        if (id == null) return Optional.empty();
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id.trim().toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapUser(conn, rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[DATABASE ERROR] findUserById: " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> findUserByEmail(String email) {
        if (email == null) return Optional.empty();
        String sql = "SELECT * FROM users WHERE LOWER(email) = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email.trim().toLowerCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapUser(conn, rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[DATABASE ERROR] findUserByEmail: " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public List<User> getAllUsers() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY id";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapUser(conn, rs));
            }
        } catch (SQLException e) {
            System.err.println("[DATABASE ERROR] getAllUsers: " + e.getMessage());
        }
        return list;
    }

    @Override
    public List<User> getUsersByRole(Role role) {
        if (role == null) return Collections.emptyList();
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE role = ? ORDER BY id";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, role.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapUser(conn, rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[DATABASE ERROR] getUsersByRole: " + e.getMessage());
        }
        return list;
    }

    @Override
    public boolean deleteUser(String id) {
        if (id == null) return false;
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id.trim().toUpperCase());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[DATABASE ERROR] deleteUser: " + e.getMessage());
            return false;
        }
    }

    private User mapUser(Connection conn, ResultSet rs) throws SQLException {
        String id = rs.getString("id");
        String name = rs.getString("name");
        String email = rs.getString("email");
        String password = rs.getString("password");
        String roleStr = rs.getString("role");

        if ("ADMIN".equalsIgnoreCase(roleStr)) {
            String dept = rs.getString("department");
            String level = rs.getString("admin_level");
            return new Admin(id, name, email, password, dept, level);
        } else if ("INSTRUCTOR".equalsIgnoreCase(roleStr)) {
            String dept = rs.getString("department");
            String spec = rs.getString("specialization");
            Instructor ins = new Instructor(id, name, email, password, dept, spec);

            // Load assigned courses
            try (PreparedStatement ps = conn.prepareStatement("SELECT course_code FROM instructor_courses WHERE instructor_id = ?")) {
                ps.setString(1, id);
                try (ResultSet crs = ps.executeQuery()) {
                    while (crs.next()) {
                        ins.assignCourse(crs.getString("course_code"));
                    }
                }
            }
            return ins;
        } else {
            String major = rs.getString("major");
            int year = rs.getInt("year_of_study");
            int sem = rs.getInt("current_semester");
            String phone = rs.getString("contact_number");
            return new Student(id, name, email, password, major, year, sem, phone);
        }
    }

    // ==========================================
    // COURSE OPERATIONS
    // ==========================================

    @Override
    public void saveCourse(Course course) {
        if (course == null) return;
        String sql = "INSERT INTO courses (course_code, title, description, credits, department, max_capacity, instructor_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE title=VALUES(title), description=VALUES(description), " +
                "credits=VALUES(credits), department=VALUES(department), max_capacity=VALUES(max_capacity), instructor_id=VALUES(instructor_id)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, course.getCourseCode().toUpperCase());
            ps.setString(2, course.getTitle());
            ps.setString(3, course.getDescription());
            ps.setInt(4, course.getCredits());
            ps.setString(5, course.getDepartment());
            ps.setInt(6, course.getMaxCapacity());
            if (course.getInstructorId() != null && !course.getInstructorId().trim().isEmpty()) {
                ps.setString(7, course.getInstructorId().trim().toUpperCase());
            } else {
                ps.setNull(7, Types.VARCHAR);
            }
            ps.executeUpdate();

            // Sync Prerequisites
            try (PreparedStatement del = conn.prepareStatement("DELETE FROM course_prerequisites WHERE course_code = ?")) {
                del.setString(1, course.getCourseCode());
                del.executeUpdate();
            }
            if (!course.getPrerequisites().isEmpty()) {
                try (PreparedStatement ins = conn.prepareStatement("INSERT IGNORE INTO course_prerequisites (course_code, prerequisite_code) VALUES (?, ?)")) {
                    for (String pre : course.getPrerequisites()) {
                        ins.setString(1, course.getCourseCode());
                        ins.setString(2, pre);
                        ins.addBatch();
                    }
                    ins.executeBatch();
                }
            }

            // Sync Instructor Assignment
            if (course.getInstructorId() != null) {
                try (PreparedStatement ins = conn.prepareStatement("INSERT IGNORE INTO instructor_courses (instructor_id, course_code) VALUES (?, ?)")) {
                    ins.setString(1, course.getInstructorId());
                    ins.setString(2, course.getCourseCode());
                    ins.executeUpdate();
                }
            }
        } catch (SQLException e) {
            System.err.println("[DATABASE ERROR] saveCourse: " + e.getMessage());
        }
    }

    @Override
    public Optional<Course> findCourseByCode(String code) {
        if (code == null) return Optional.empty();
        String clean = code.trim().toUpperCase();

        String sql = "SELECT * FROM courses WHERE course_code = ? OR course_code = ? OR course_code = ?";
        String alt1 = clean.startsWith("CS") && !clean.startsWith("CSE") ? clean.replaceFirst("CS", "CSE") : clean;
        String alt2 = clean.startsWith("CSE") ? clean.replaceFirst("CSE", "CS") : clean;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, clean);
            ps.setString(2, alt1);
            ps.setString(3, alt2);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapCourse(conn, rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[DATABASE ERROR] findCourseByCode: " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public List<Course> getAllCourses() {
        List<Course> list = new ArrayList<>();
        String sql = "SELECT * FROM courses ORDER BY course_code";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapCourse(conn, rs));
            }
        } catch (SQLException e) {
            System.err.println("[DATABASE ERROR] getAllCourses: " + e.getMessage());
        }
        return list;
    }

    @Override
    public boolean deleteCourse(String code) {
        if (code == null) return false;
        String sql = "DELETE FROM courses WHERE course_code = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code.trim().toUpperCase());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[DATABASE ERROR] deleteCourse: " + e.getMessage());
            return false;
        }
    }

    private Course mapCourse(Connection conn, ResultSet rs) throws SQLException {
        String code = rs.getString("course_code");
        String title = rs.getString("title");
        String desc = rs.getString("description");
        int credits = rs.getInt("credits");
        String dept = rs.getString("department");
        int cap = rs.getInt("max_capacity");
        String instructorId = rs.getString("instructor_id");

        Course c = new Course(code, title, desc, credits, dept, cap, instructorId);

        // Load Prerequisites
        try (PreparedStatement ps = conn.prepareStatement("SELECT prerequisite_code FROM course_prerequisites WHERE course_code = ?")) {
            ps.setString(1, code);
            try (ResultSet pr = ps.executeQuery()) {
                while (pr.next()) {
                    c.addPrerequisite(pr.getString("prerequisite_code"));
                }
            }
        }
        return c;
    }

    // ==========================================
    // ENROLLMENT OPERATIONS
    // ==========================================

    @Override
    public void saveEnrollment(Enrollment enrollment) {
        if (enrollment == null) return;
        String sql = "INSERT INTO enrollments (enrollment_id, student_id, course_code, semester, score, grade, status, enrollment_date) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE semester=VALUES(semester), score=VALUES(score), grade=VALUES(grade), status=VALUES(status)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, enrollment.getEnrollmentId().toUpperCase());
            ps.setString(2, enrollment.getStudentId().toUpperCase());
            ps.setString(3, enrollment.getCourseCode().toUpperCase());
            ps.setString(4, enrollment.getSemester());
            ps.setDouble(5, enrollment.getScore());
            ps.setString(6, enrollment.getGrade().name());
            ps.setString(7, enrollment.getStatus().name());
            ps.setString(8, enrollment.getEnrollmentDate());

            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DATABASE ERROR] saveEnrollment: " + e.getMessage());
        }
    }

    @Override
    public Optional<Enrollment> findEnrollmentById(String id) {
        if (id == null) return Optional.empty();
        String sql = "SELECT * FROM enrollments WHERE enrollment_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id.trim().toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapEnrollment(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[DATABASE ERROR] findEnrollmentById: " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public List<Enrollment> getAllEnrollments() {
        List<Enrollment> list = new ArrayList<>();
        String sql = "SELECT * FROM enrollments ORDER BY enrollment_id";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapEnrollment(rs));
            }
        } catch (SQLException e) {
            System.err.println("[DATABASE ERROR] getAllEnrollments: " + e.getMessage());
        }
        return list;
    }

    @Override
    public List<Enrollment> getEnrollmentsByStudent(String studentId) {
        if (studentId == null) return Collections.emptyList();
        List<Enrollment> list = new ArrayList<>();
        String sql = "SELECT * FROM enrollments WHERE student_id = ? ORDER BY course_code";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, studentId.trim().toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapEnrollment(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[DATABASE ERROR] getEnrollmentsByStudent: " + e.getMessage());
        }
        return list;
    }

    @Override
    public List<Enrollment> getEnrollmentsByCourse(String courseCode) {
        if (courseCode == null) return Collections.emptyList();
        List<Enrollment> list = new ArrayList<>();
        String sql = "SELECT * FROM enrollments WHERE course_code = ? ORDER BY student_id";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, courseCode.trim().toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapEnrollment(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[DATABASE ERROR] getEnrollmentsByCourse: " + e.getMessage());
        }
        return list;
    }

    @Override
    public boolean deleteEnrollment(String enrollmentId) {
        if (enrollmentId == null) return false;
        String sql = "DELETE FROM enrollments WHERE enrollment_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, enrollmentId.trim().toUpperCase());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[DATABASE ERROR] deleteEnrollment: " + e.getMessage());
            return false;
        }
    }

    private Enrollment mapEnrollment(ResultSet rs) throws SQLException {
        String id = rs.getString("enrollment_id");
        String stuId = rs.getString("student_id");
        String courseCode = rs.getString("course_code");
        String semester = rs.getString("semester");
        double score = rs.getDouble("score");
        Grade grade = Grade.fromString(rs.getString("grade"));
        EnrollmentStatus status = EnrollmentStatus.fromString(rs.getString("status"));
        String date = rs.getString("enrollment_date");

        return new Enrollment(id, stuId, courseCode, semester, score, grade, status, date);
    }

    // ==========================================
    // PERSISTENCE LIFECYCLE
    // ==========================================

    @Override
    public void saveToStorage() {
        // Direct MySQL commit happens automatically on statement execution
    }

    @Override
    public void loadFromStorage() {
        // Data is always fetched fresh from MySQL via JDBC
    }
}

-- ============================================================================
-- EduTrack: University Course Management System (GCET)
-- Relational Database Schema & Data Seeding Script for MySQL
-- ============================================================================

CREATE DATABASE IF NOT EXISTS edutrack_db;
USE edutrack_db;

-- 1. Disable foreign key checks for clean recreation
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS enrollments;
DROP TABLE IF EXISTS course_prerequisites;
DROP TABLE IF EXISTS instructor_courses;
DROP TABLE IF EXISTS courses;
DROP TABLE IF EXISTS users;

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================================
-- 2. TABLE: users (Administrators, Instructors, Students)
-- ============================================================================
CREATE TABLE users (
    id VARCHAR(20) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL, -- 'ADMIN', 'INSTRUCTOR', 'STUDENT'
    
    -- Admin specific fields
    department VARCHAR(100),
    admin_level VARCHAR(50),
    
    -- Instructor specific fields
    specialization VARCHAR(100),
    
    -- Student specific fields
    major VARCHAR(100),
    year_of_study INT DEFAULT 1,
    current_semester INT DEFAULT 1,
    contact_number VARCHAR(20),
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================================
-- 3. TABLE: courses
-- ============================================================================
CREATE TABLE courses (
    course_code VARCHAR(20) PRIMARY KEY,
    title VARCHAR(150) NOT NULL,
    description TEXT,
    credits INT NOT NULL CHECK (credits BETWEEN 1 AND 8),
    department VARCHAR(100) NOT NULL,
    max_capacity INT NOT NULL DEFAULT 40,
    instructor_id VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_course_instructor FOREIGN KEY (instructor_id) 
        REFERENCES users(id) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================================
-- 4. TABLE: course_prerequisites
-- ============================================================================
CREATE TABLE course_prerequisites (
    course_code VARCHAR(20) NOT NULL,
    prerequisite_code VARCHAR(20) NOT NULL,
    PRIMARY KEY (course_code, prerequisite_code),
    CONSTRAINT fk_prereq_course FOREIGN KEY (course_code) 
        REFERENCES courses(course_code) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_prereq_required FOREIGN KEY (prerequisite_code) 
        REFERENCES courses(course_code) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================================
-- 5. TABLE: instructor_courses (Teaching Assignments)
-- ============================================================================
CREATE TABLE instructor_courses (
    instructor_id VARCHAR(20) NOT NULL,
    course_code VARCHAR(20) NOT NULL,
    PRIMARY KEY (instructor_id, course_code),
    CONSTRAINT fk_ins_user FOREIGN KEY (instructor_id) 
        REFERENCES users(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_ins_course FOREIGN KEY (course_code) 
        REFERENCES courses(course_code) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================================
-- 6. TABLE: enrollments (Registrations & Grades)
-- ============================================================================
CREATE TABLE enrollments (
    enrollment_id VARCHAR(20) PRIMARY KEY,
    student_id VARCHAR(20) NOT NULL,
    course_code VARCHAR(20) NOT NULL,
    semester VARCHAR(30) NOT NULL,
    score DECIMAL(5,2) DEFAULT -1.00,
    grade VARCHAR(15) DEFAULT 'IN_PROGRESS',
    status VARCHAR(20) DEFAULT 'ACTIVE', -- 'ACTIVE', 'COMPLETED', 'DROPPED'
    enrollment_date VARCHAR(30),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_enr_student FOREIGN KEY (student_id) 
        REFERENCES users(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_enr_course FOREIGN KEY (course_code) 
        REFERENCES courses(course_code) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================================
-- 7. SEED DATA INSERTIONS
-- ============================================================================

-- Administrators
INSERT INTO users (id, name, email, password, role, department, admin_level) VALUES
('ADM101', 'Dr. Sarah Jenkins', 'admin@gcet.edu.in', 'admin123', 'ADMIN', 'Academic Affairs', 'University Registrar'),
('ADM102', 'Mark Henderson', 'mark@gcet.edu.in', 'admin123', 'ADMIN', 'Faculty of Engineering', 'Department Dean');

-- Faculty Instructors
INSERT INTO users (id, name, email, password, role, department, specialization) VALUES
('INS201', 'Prof. Alan Turing', 'turing@gcet.edu.in', 'prof123', 'INSTRUCTOR', 'CSE', 'Algorithms & Theory'),
('INS202', 'Prof. Grace Hopper', 'hopper@gcet.edu.in', 'prof123', 'INSTRUCTOR', 'CSE', 'Programming Systems & Compilers'),
('INS203', 'Dr. Claude Shannon', 'shannon@gcet.edu.in', 'prof123', 'INSTRUCTOR', 'Electrical & Computer Eng', 'Networks & Information Theory');

-- Students (Year 2 CSE)
INSERT INTO users (id, name, email, password, role, major, year_of_study, current_semester, contact_number) VALUES
('STU301', 'Harika Mukkamula', 'harika@gcet.edu.in', 'student123', 'STUDENT', 'CSE', 2, 3, '9876543210'),
('STU302', 'Rahul Sharma', 'rahul@gcet.edu.in', 'student123', 'STUDENT', 'CSE', 2, 3, '9876543211'),
('STU303', 'Priya Patel', 'priya@gcet.edu.in', 'student123', 'STUDENT', 'CSE', 2, 3, '9876543212'),
('STU304', 'David Miller', 'david@gcet.edu.in', 'student123', 'STUDENT', 'Data Science', 1, 1, '9876543213'),
('STU305', 'TUPAKULA SOWMYA', '25r11a05lp@gcet.edu.in', '25R11A05LP', 'STUDENT', 'CSE', 2, 3, '6302038180');

-- Courses
INSERT INTO courses (course_code, title, description, credits, department, max_capacity, instructor_id) VALUES
('CSE101', 'Introduction to Java OOP', 'Core principles of OOP, encapsulation, polymorphism, and Java fundamentals', 4, 'CSE', 45, 'INS202'),
('CSE102', 'Data Structures & Algorithms', 'Analysis and implementation of lists, trees, graphs, hashing, and sorting', 4, 'CSE', 40, 'INS201'),
('CSE201', 'Software Design Patterns', 'GoF design patterns, UML, refactoring, and clean architecture', 3, 'CSE', 35, 'INS202'),
('CSE301', 'Database Management Systems', 'Relational algebra, SQL, normalization, transaction processing, and indexing', 4, 'CSE', 40, 'INS201'),
('CSE405', 'Cloud Computing & Distributed Systems', 'Microservices, virtualization, distributed storage, and AWS/GCP architecture', 3, 'CSE', 30, 'INS201'),
('EE205', 'Computer Networks & Protocols', 'Layered network architecture, TCP/IP, routing protocols, and socket programming', 3, 'Electrical & Computer Eng', 30, 'INS203');

-- Prerequisites
INSERT INTO course_prerequisites (course_code, prerequisite_code) VALUES
('CSE102', 'CSE101'),
('CSE201', 'CSE101'),
('CSE301', 'CSE102'),
('CSE405', 'CSE301'),
('EE205', 'CSE101');

-- Instructor Course Assignments
INSERT INTO instructor_courses (instructor_id, course_code) VALUES
('INS201', 'CSE102'),
('INS201', 'CSE301'),
('INS201', 'CSE405'),
('INS202', 'CSE101'),
('INS202', 'CSE201'),
('INS203', 'EE205');

-- Enrollments & Grades
INSERT INTO enrollments (enrollment_id, student_id, course_code, semester, score, grade, status, enrollment_date) VALUES
('ENR-1001', 'STU301', 'CSE101', 'Spring 2026', 93.50, 'A', 'COMPLETED', '2026-01-15 10:00'),
('ENR-1002', 'STU301', 'CSE102', 'Fall 2026', 86.00, 'B', 'COMPLETED', '2026-07-20 11:30'),
('ENR-1003', 'STU301', 'CSE201', 'Fall 2026', -1.00, 'IN_PROGRESS', 'ACTIVE', '2026-08-01 09:15'),
('ENR-1009', 'STU301', 'CSE301', 'Fall 2026', 94.00, 'A', 'COMPLETED', '2026-10-16 19:38'),
('ENR-1004', 'STU302', 'CSE101', 'Spring 2026', 78.00, 'C', 'COMPLETED', '2026-01-16 14:00'),
('ENR-1005', 'STU302', 'CSE102', 'Fall 2026', -1.00, 'IN_PROGRESS', 'ACTIVE', '2026-08-02 10:00'),
('ENR-1006', 'STU303', 'CSE101', 'Spring 2026', 96.00, 'A', 'COMPLETED', '2026-01-15 10:30'),
('ENR-1007', 'STU303', 'CSE102', 'Fall 2026', 91.00, 'A', 'COMPLETED', '2026-07-21 14:30'),
('ENR-1008', 'STU303', 'CSE301', 'Fall 2026', -1.00, 'IN_PROGRESS', 'ACTIVE', '2026-08-05 16:00');

-- ============================================================================
-- Verification Queries
-- ============================================================================
SELECT 'users' AS table_name, COUNT(*) AS count FROM users
UNION ALL
SELECT 'courses', COUNT(*) FROM courses
UNION ALL
SELECT 'course_prerequisites', COUNT(*) FROM course_prerequisites
UNION ALL
SELECT 'instructor_courses', COUNT(*) FROM instructor_courses
UNION ALL
SELECT 'enrollments', COUNT(*) FROM enrollments;

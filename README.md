# EduTrack: Object-Oriented Course Management System in Java
## Integrated with MySQL Relational Database & Java Swing GUI

**EduTrack** is an enterprise-grade Course Management System engineered for **Geethanjali College of Engineering & Technology (GCET)**. It features pure **Object-Oriented Programming (OOP)**, an **interactive Java Swing GUI**, and direct integration with a **MySQL Relational Database** (`edutrack_db`).

---

## 🗄️ MySQL Database Architecture (`edutrack_db`)

The entire system persists to and queries live from a relational database hosted on MySQL Server:

### 1. Database Entity-Relationship Overview
- **`users` Table**: Polymorphic storage of Administrators, Instructors, and Students with role-specific attributes (`department`, `admin_level`, `specialization`, `major`, `year_of_study`, `current_semester`, `contact_number`).
- **`courses` Table**: Course catalog including course code, course title, description, credits (1–8), department, maximum seat capacity, and foreign key to `users(id)` representing the assigned faculty instructor.
- **`course_prerequisites` Table**: Composite key table `(course_code, prerequisite_code)` enforcing academic dependency constraints with `CASCADE` updates.
- **`instructor_courses` Table**: Composite key table `(instructor_id, course_code)` managing teaching assignments.
- **`enrollments` Table**: Primary relationship tracking student registrations, semesters, numeric scores ($0.0 - 100.0$), letter grades (`A+`, `A`, `B`, `C`, `D`, `F`), and status (`ACTIVE`, `COMPLETED`, `DROPPED`).

### 2. SQL Schema Script
The full DDL and DML script is located in [`schema.sql`](file:///c:/Users/harik/Desktop/EduTrack/schema.sql) and can be executed anytime in MySQL Command Line or MySQL Workbench:
```sql
CREATE DATABASE IF NOT EXISTS edutrack_db;
USE edutrack_db;
```

---

## 🏛️ Pure OOP Principles & Design Patterns

| Principle / Pattern | Implementation in EduTrack |
|---|---|
| **Abstraction** | Abstract base class `User` and interface contract `DataStore`. Business logic in services is completely decoupled from whether data resides in MySQL or cache. |
| **Encapsulation** | Private attributes, defensive copying, score validation ($0–100$), capacity constraints, and unmodifiable prerequisite collections. |
| **Inheritance** | Role hierarchy (`User` $\rightarrow$ `Admin`, `Instructor`, `Student`) sharing common identity/auth logic while specializing domain behaviors. |
| **Polymorphism** | Dynamic method dispatch (`getRoleSpecificDetails()`, `getAffiliation()`), overloaded methods, and uniform credential handling. |
| **Factory Pattern** | [`DataStoreFactory`](file:///c:/Users/harik/Desktop/EduTrack/src/com/edutrack/data/DataStoreFactory.java) automatically initializes `MySqlDataStore` when MySQL is online, with seamless fallback to `InMemoryDataStore` if offline. |

---

## 📂 Project Directory Structure

```
EduTrack/
├── bin/                          # Compiled .class bytecode
├── data/                         # CSV backup and synchronization files
├── lib/                          # External libraries
│   └── mysql-connector-j.jar     # MySQL JDBC Driver (8.3.0)
├── src/
│   └── com/edutrack/
│       ├── EduTrackApp.java      # Interactive console CLI entry point
│       ├── data/                 # Database & persistence layer
│       │   ├── DatabaseConnection.java # JDBC connection manager with fallback
│       │   ├── DataStore.java          # Repository interface contract
│       │   ├── DataStoreFactory.java   # Factory choosing MySQL vs fallback
│       │   ├── MySqlDataStore.java     # Live SQL database repository
│       │   ├── InMemoryDataStore.java  # Collections-backed repository
│       │   └── FileStorageManager.java # CSV parser and serializer
│       ├── gui/                  # Java Swing Panels & GUI architecture
│       │   ├── EduTrackGUI.java              # Main Swing application window
│       │   ├── LoginPanel.java               # Auth panel with live DB indicator
│       │   ├── AdminDashboardPanel.java      # Administrator dashboard
│       │   ├── InstructorDashboardPanel.java # Faculty portal
│       │   ├── StudentDashboardPanel.java    # Student self-service portal
│       │   ├── RegistrationDialog.java       # Registration dialog
│       │   └── UITheme.java                  # Modern university styling
│       ├── exception/            # Custom domain exceptions
│       ├── model/                # User, Admin, Instructor, Student, Course, Enrollment, Grade
│       ├── service/              # UserService, CourseService, EnrollmentService, ReportService
│       └── util/                 # ConsoleUtils & ValidationUtils
├── schema.sql                    # MySQL Table creation & data seeding script
├── db.properties                 # MySQL connection configuration
├── compile.bat                   # 1-click compilation script
├── run.bat                       # 1-click Swing GUI launcher (MySQL mode)
├── run-gui.bat                   # Dedicated GUI launcher
├── run-console.bat               # Dedicated Console launcher
└── README.md                     # Complete project documentation
```

---

## 🔑 Pre-Loaded Demo Accounts

| Role | User ID | Registered Email | Password | Academic Status / Designation |
|---|---|---|---|---|
| **Admin** | `ADM101` | `admin@gcet.edu.in` | `admin123` | Dr. Sarah Jenkins (University Registrar) |
| **Admin** | `ADM102` | `mark@gcet.edu.in` | `admin123` | Mark Henderson (Department Dean) |
| **Instructor** | `INS201` | `turing@gcet.edu.in` | `prof123` | Prof. Alan Turing (CSE Dept - Algorithms) |
| **Instructor** | `INS202` | `hopper@gcet.edu.in` | `prof123` | Prof. Grace Hopper (CSE Dept - OOP & Compilers) |
| **Student** | `STU301` | `harika@gcet.edu.in` | `student123` | Harika Mukkamula (**Year 2, CSE**) |
| **Student** | `STU302` | `rahul@gcet.edu.in` | `student123` | Rahul Sharma (**Year 2, CSE**) |

---

## ⚙️ Database Configuration (`db.properties`)

```properties
db.url=jdbc:mysql://localhost:3306/edutrack_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
db.user=KU
db.password=25r11a05ku
db.driver=com.mysql.cj.jdbc.Driver

# Fallback credentials if presenting on another computer
db.fallback.user=root
db.fallback.password=root
```

---

## 🚀 How to Run

### Method 1: Double-Click Batch Files (Recommended)
- **Launch Swing GUI Panel (Connected to MySQL)**: Double-click [`run.bat`](file:///c:/Users/harik/Desktop/EduTrack/run.bat) or [`run-gui.bat`](file:///c:/Users/harik/Desktop/EduTrack/run-gui.bat).
- **Launch Console CLI (Connected to MySQL)**: Double-click [`run-console.bat`](file:///c:/Users/harik/Desktop/EduTrack/run-console.bat).
- **Recompile**: Double-click [`compile.bat`](file:///c:/Users/harik/Desktop/EduTrack/compile.bat).

### Method 2: Command Line
```powershell
cd c:\Users\harik\Desktop\EduTrack

# Compile
.\compile.bat

# Run Swing GUI connected to MySQL
java -cp "bin;lib/*" com.edutrack.gui.EduTrackGUI data
```

package com.edutrack.gui;

import com.edutrack.exception.CourseCapacityExceededException;
import com.edutrack.exception.DuplicateEnrollmentException;
import com.edutrack.exception.PrerequisiteNotMetException;
import com.edutrack.model.*;
import com.edutrack.service.CourseService;
import com.edutrack.service.EnrollmentService;
import com.edutrack.service.UserService;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * StudentDashboardPanel: Student self-service portal for course registration,
 * active timetable viewing, course dropping, and official transcript inspection.
 */
public class StudentDashboardPanel extends JPanel {
    private final Frame parentFrame;
    private final UserService userService;
    private final CourseService courseService;
    private final EnrollmentService enrollmentService;
    private final Runnable onLogout;

    private Student currentStudent;

    private JPanel headerContainer;
    private JTabbedPane tabbedPane;

    // Tab 1: Course Catalog
    private JTable tblCatalog;
    private DefaultTableModel modelCatalog;
    private JTextField txtSemester;

    // Tab 2: Active Enrollments
    private JTable tblActive;
    private DefaultTableModel modelActive;

    // Tab 3: Academic Transcript
    private JTable tblTranscript;
    private DefaultTableModel modelTranscript;
    private JLabel lblGpaSummary;

    // Tab 4: Profile
    private JPanel pnlProfile;

    public StudentDashboardPanel(Frame parentFrame, UserService userService, CourseService courseService,
                                 EnrollmentService enrollmentService, Runnable onLogout) {
        this.parentFrame = parentFrame;
        this.userService = userService;
        this.courseService = courseService;
        this.enrollmentService = enrollmentService;
        this.onLogout = onLogout;

        setLayout(new BorderLayout());
        setBackground(UITheme.BG_LIGHT);

        initUI();
    }

    public void setCurrentStudent(Student student) {
        this.currentStudent = student;
        refreshHeader();
        refreshAllData();
    }

    private void refreshHeader() {
        headerContainer.removeAll();
        if (currentStudent != null) {
            double gpa = enrollmentService.calculateGPA(currentStudent.getId());
            JPanel banner = UITheme.createHeaderBanner(
                    "EduTrack | Student Portal",
                    currentStudent.getMajor() + " • Year " + currentStudent.getYearOfStudy() + " (Sem " + currentStudent.getCurrentSemester() + ") • CGPA: " + String.format("%.2f", gpa) + " / 4.0",
                    "Student (" + currentStudent.getId() + ")",
                    currentStudent.getName(),
                    onLogout
            );
            headerContainer.add(banner, BorderLayout.CENTER);
        }
        headerContainer.revalidate();
        headerContainer.repaint();
    }

    private void initUI() {
        headerContainer = new JPanel(new BorderLayout());
        add(headerContainer, BorderLayout.NORTH);

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UITheme.FONT_BOLD);
        tabbedPane.setBackground(Color.WHITE);

        tabbedPane.addTab("📚 Course Catalog & Registration", createCatalogPanel());
        tabbedPane.addTab("🗓️ My Active Enrollments", createActiveEnrollmentsPanel());
        tabbedPane.addTab("📄 Official Transcript & GPA", createTranscriptPanel());
        tabbedPane.addTab("👤 My Student Profile", createProfilePanel());

        add(tabbedPane, BorderLayout.CENTER);
    }

    // ==========================================
    // TAB 1: COURSE CATALOG & ENROLLMENT
    // ==========================================
    private JPanel createCatalogPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(UITheme.BG_LIGHT);
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        toolbar.setOpaque(false);

        JLabel lblSem = new JLabel("Target Semester:");
        lblSem.setFont(UITheme.FONT_BOLD);
        txtSemester = new JTextField("Fall 2026", 10);
        txtSemester.setFont(UITheme.FONT_BODY);
        txtSemester.setBorder(new CompoundBorder(
                new LineBorder(UITheme.BORDER_DARK, 1),
                new EmptyBorder(6, 8, 6, 8)
        ));

        JButton btnEnroll = UITheme.createPrimaryButton("➕ Enroll in Selected Course");
        btnEnroll.addActionListener(e -> handleEnrollCourse());

        JButton btnRefresh = UITheme.createSecondaryButton("🔄 Refresh");
        btnRefresh.addActionListener(e -> loadCatalogData());

        toolbar.add(lblSem);
        toolbar.add(txtSemester);
        toolbar.add(btnEnroll);
        toolbar.add(btnRefresh);
        panel.add(toolbar, BorderLayout.NORTH);

        // Table
        String[] cols = {"Course Code", "Title", "Credits", "Department", "Faculty Instructor", "Seats (Filled/Max)", "Prerequisites"};
        modelCatalog = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        tblCatalog = new JTable(modelCatalog);
        UITheme.styleTable(tblCatalog);

        panel.add(new JScrollPane(tblCatalog), BorderLayout.CENTER);
        return panel;
    }

    private void loadCatalogData() {
        modelCatalog.setRowCount(0);
        List<Course> courses = courseService.getAllCourses();
        for (Course c : courses) {
            String instructorName = "Unassigned";
            if (c.getInstructorId() != null) {
                instructorName = userService.getAllUsers().stream()
                        .filter(u -> u.getId().equalsIgnoreCase(c.getInstructorId()))
                        .findFirst()
                        .map(User::getName)
                        .orElse(c.getInstructorId());
            }

            int active = courseService.getActiveEnrollmentCount(c.getCourseCode());
            String seats = active + " / " + c.getMaxCapacity();
            String prereqs = c.getPrerequisites().isEmpty() ? "None" : String.join(", ", c.getPrerequisites());

            modelCatalog.addRow(new Object[]{
                    c.getCourseCode(),
                    c.getTitle(),
                    c.getCredits(),
                    c.getDepartment(),
                    instructorName,
                    seats,
                    prereqs
            });
        }
    }

    private void handleEnrollCourse() {
        int selected = tblCatalog.getSelectedRow();
        if (selected < 0) {
            JOptionPane.showMessageDialog(this, "Please select a course to enroll from the catalog.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String courseCode = modelCatalog.getValueAt(selected, 0).toString();
        String semester = txtSemester.getText().trim();
        if (semester.isEmpty()) semester = "Fall 2026";

        try {
            Enrollment enrollment = enrollmentService.enrollStudent(currentStudent.getId(), courseCode, semester);
            JOptionPane.showMessageDialog(this,
                    "Congratulations! Successfully enrolled in course " + courseCode + "!\n" +
                            "Enrollment Reference ID: " + enrollment.getEnrollmentId() + " (" + semester + ")",
                    "Enrollment Confirmed", JOptionPane.INFORMATION_MESSAGE);

            loadCatalogData();
            loadActiveEnrollments();
            loadTranscriptData();
            refreshHeader();
        } catch (DuplicateEnrollmentException ex) {
            JOptionPane.showMessageDialog(this,
                    "Registration Blocked:\nYou are already actively enrolled in course " + courseCode + ".",
                    "Duplicate Enrollment", JOptionPane.WARNING_MESSAGE);
        } catch (CourseCapacityExceededException ex) {
            JOptionPane.showMessageDialog(this,
                    "Class Full:\n" + ex.getMessage(),
                    "Course Capacity Reached", JOptionPane.WARNING_MESSAGE);
        } catch (PrerequisiteNotMetException ex) {
            JOptionPane.showMessageDialog(this,
                    "Prerequisite Missing:\n" + ex.getMessage(),
                    "Prerequisite Requirement", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Registration Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ==========================================
    // TAB 2: ACTIVE ENROLLMENTS & TIMETABLE
    // ==========================================
    private JPanel createActiveEnrollmentsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(UITheme.BG_LIGHT);
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        toolbar.setOpaque(false);

        JButton btnDrop = UITheme.createDangerButton("✕ Drop Selected Course");
        btnDrop.addActionListener(e -> handleDropCourse());

        JButton btnRefresh = UITheme.createSecondaryButton("🔄 Refresh");
        btnRefresh.addActionListener(e -> loadActiveEnrollments());

        toolbar.add(btnDrop);
        toolbar.add(btnRefresh);
        panel.add(toolbar, BorderLayout.NORTH);

        String[] cols = {"Enrollment ID", "Course Code", "Course Title", "Credits", "Instructor", "Semester", "Status"};
        modelActive = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        tblActive = new JTable(modelActive);
        UITheme.styleTable(tblActive);

        panel.add(new JScrollPane(tblActive), BorderLayout.CENTER);
        return panel;
    }

    private void loadActiveEnrollments() {
        if (currentStudent == null) return;
        modelActive.setRowCount(0);

        List<Enrollment> list = enrollmentService.getStudentEnrollments(currentStudent.getId()).stream()
                .filter(e -> e.getStatus() == EnrollmentStatus.ACTIVE)
                .collect(Collectors.toList());

        for (Enrollment e : list) {
            Course c = courseService.getAllCourses().stream()
                    .filter(crs -> crs.getCourseCode().equalsIgnoreCase(e.getCourseCode()))
                    .findFirst().orElse(null);

            String title = c != null ? c.getTitle() : "N/A";
            String credits = c != null ? String.valueOf(c.getCredits()) : "-";
            String instructor = "Unassigned";
            if (c != null && c.getInstructorId() != null) {
                instructor = userService.getAllUsers().stream()
                        .filter(u -> u.getId().equalsIgnoreCase(c.getInstructorId()))
                        .findFirst()
                        .map(User::getName)
                        .orElse(c.getInstructorId());
            }

            modelActive.addRow(new Object[]{
                    e.getEnrollmentId(),
                    e.getCourseCode(),
                    title,
                    credits,
                    instructor,
                    e.getSemester(),
                    e.getStatus().name()
            });
        }
    }

    private void handleDropCourse() {
        int selected = tblActive.getSelectedRow();
        if (selected < 0) {
            JOptionPane.showMessageDialog(this, "Please select an active course to drop.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String enrId = modelActive.getValueAt(selected, 0).toString();
        String code = modelActive.getValueAt(selected, 1).toString();
        String title = modelActive.getValueAt(selected, 2).toString();

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to drop course " + code + " (" + title + ")?",
                "Confirm Course Drop", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                enrollmentService.dropCourse(enrId, currentStudent.getId());
                JOptionPane.showMessageDialog(this, "Course " + code + " has been successfully dropped.", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadActiveEnrollments();
                loadCatalogData();
                loadTranscriptData();
                refreshHeader();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ==========================================
    // TAB 3: ACADEMIC TRANSCRIPT & GPA
    // ==========================================
    private JPanel createTranscriptPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(UITheme.BG_LIGHT);
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        String[] cols = {"Course Code", "Course Title", "Credits", "Semester", "Status", "Score / Marks", "Letter Grade", "Grade Points"};
        modelTranscript = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        tblTranscript = new JTable(modelTranscript);
        UITheme.styleTable(tblTranscript);

        panel.add(new JScrollPane(tblTranscript), BorderLayout.CENTER);

        // Bottom Summary Card
        JPanel cardSummary = UITheme.createCardPanel();
        cardSummary.setLayout(new BorderLayout());

        lblGpaSummary = new JLabel("Calculating Academic Standing...");
        lblGpaSummary.setFont(UITheme.FONT_SUBHEADER);
        cardSummary.add(lblGpaSummary, BorderLayout.CENTER);

        JButton btnRefresh = UITheme.createSecondaryButton("🔄 Refresh Transcript");
        btnRefresh.addActionListener(e -> loadTranscriptData());
        cardSummary.add(btnRefresh, BorderLayout.EAST);

        panel.add(cardSummary, BorderLayout.SOUTH);
        return panel;
    }

    private void loadTranscriptData() {
        if (currentStudent == null) return;
        modelTranscript.setRowCount(0);

        List<Enrollment> list = enrollmentService.getStudentEnrollments(currentStudent.getId());
        int totalEarnedCredits = 0;

        for (Enrollment e : list) {
            Course c = courseService.getAllCourses().stream()
                    .filter(crs -> crs.getCourseCode().equalsIgnoreCase(e.getCourseCode()))
                    .findFirst().orElse(null);

            String title = c != null ? c.getTitle() : "N/A";
            int credits = c != null ? c.getCredits() : 3;
            String scoreStr = e.getScore() >= 0 ? String.format("%.1f", e.getScore()) : "Pending";
            String pointsStr = e.isGraded() ? String.format("%.1f", e.getGrade().getGradePoint()) : "-";

            if (e.getStatus() == EnrollmentStatus.COMPLETED && e.getGrade().isPassing()) {
                totalEarnedCredits += credits;
            }

            modelTranscript.addRow(new Object[]{
                    e.getCourseCode(),
                    title,
                    credits,
                    e.getSemester(),
                    e.getStatus().name(),
                    scoreStr,
                    e.getGrade().getLetter(),
                    pointsStr
            });
        }

        double gpa = enrollmentService.calculateGPA(currentStudent.getId());
        String standing = gpa >= 3.5 ? "Dean's Honor List 🏆" : (gpa >= 2.0 ? "Good Academic Standing" : "Academic Probation Warning");

        lblGpaSummary.setText("<html><b>Total Credits Earned:</b> " + totalEarnedCredits + " Credits &nbsp;|&nbsp; " +
                "<b>Cumulative GPA:</b> <span style='color: #2563EB; font-size: 14px;'>" + String.format("%.2f", gpa) + " / 4.00</span> &nbsp;|&nbsp; " +
                "<b>Academic Standing:</b> <span style='color: #16A34A;'>" + standing + "</span></html>");
    }

    // ==========================================
    // TAB 4: STUDENT PROFILE
    // ==========================================
    private JPanel createProfilePanel() {
        JPanel container = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 30));
        container.setBackground(UITheme.BG_LIGHT);

        pnlProfile = UITheme.createCardPanel();
        pnlProfile.setPreferredSize(new Dimension(540, 340));
        pnlProfile.setLayout(new GridLayout(8, 1, 0, 8));

        container.add(pnlProfile);
        return container;
    }

    private void refreshProfile() {
        pnlProfile.removeAll();
        if (currentStudent != null) {
            double gpa = enrollmentService.calculateGPA(currentStudent.getId());

            JLabel lblHdr = new JLabel("🎓 Student Academic Profile");
            lblHdr.setFont(UITheme.FONT_TITLE);
            lblHdr.setForeground(UITheme.PRIMARY);

            JLabel l1 = new JLabel("Student Roll ID:      " + currentStudent.getId());
            JLabel l2 = new JLabel("Full Legal Name:      " + currentStudent.getName());
            JLabel l3 = new JLabel("University Email:     " + currentStudent.getEmail());
            JLabel l4 = new JLabel("Academic Major:       " + currentStudent.getMajor());
            JLabel l5 = new JLabel("Academic Level:       Year " + currentStudent.getYearOfStudy() + " (Semester " + currentStudent.getCurrentSemester() + ")");
            JLabel l6 = new JLabel("Contact Phone:        " + currentStudent.getContactNumber());
            JLabel l7 = new JLabel("Cumulative CGPA:      " + String.format("%.2f / 4.00", gpa));

            l1.setFont(UITheme.FONT_BODY);
            l2.setFont(UITheme.FONT_BODY);
            l3.setFont(UITheme.FONT_BODY);
            l4.setFont(UITheme.FONT_BODY);
            l5.setFont(UITheme.FONT_BODY);
            l6.setFont(UITheme.FONT_BODY);
            l7.setFont(UITheme.FONT_BOLD);
            l7.setForeground(UITheme.ACCENT);

            pnlProfile.add(lblHdr);
            pnlProfile.add(l1);
            pnlProfile.add(l2);
            pnlProfile.add(l3);
            pnlProfile.add(l4);
            pnlProfile.add(l5);
            pnlProfile.add(l6);
            pnlProfile.add(l7);
        }
        pnlProfile.revalidate();
        pnlProfile.repaint();
    }

    public void refreshAllData() {
        loadCatalogData();
        loadActiveEnrollments();
        loadTranscriptData();
        refreshProfile();
    }
}

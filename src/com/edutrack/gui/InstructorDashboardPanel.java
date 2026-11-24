package com.edutrack.gui;

import com.edutrack.model.*;
import com.edutrack.service.CourseService;
import com.edutrack.service.EnrollmentService;
import com.edutrack.service.ReportService;
import com.edutrack.service.UserService;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * InstructorDashboardPanel: Faculty workspace for viewing teaching schedules,
 * inspecting student rosters, recording marks/grades, and viewing performance trends.
 */
public class InstructorDashboardPanel extends JPanel {
    private final Frame parentFrame;
    private final UserService userService;
    private final CourseService courseService;
    private final EnrollmentService enrollmentService;
    private final ReportService reportService;
    private final Runnable onLogout;

    private Instructor currentInstructor;

    private JPanel headerContainer;
    private JTabbedPane tabbedPane;

    // Tab 1: Assigned Courses
    private JTable tblMyCourses;
    private DefaultTableModel modelMyCourses;

    // Tab 2: Class Roster & Grading
    private JComboBox<String> cmbRosterCourses;
    private JTable tblRoster;
    private DefaultTableModel modelRoster;
    private JLabel lblSelectedStudent;
    private JTextField txtScoreInput;
    private JButton btnSubmitGrade;

    // Tab 3: Analytics
    private JComboBox<String> cmbAnalyticsCourse;
    private JLabel lblAnalyticsSummary;
    private JTable tblGradeBreakdown;
    private DefaultTableModel modelGradeBreakdown;

    // Tab 4: Profile
    private JPanel pnlProfile;

    public InstructorDashboardPanel(Frame parentFrame, UserService userService, CourseService courseService,
                                    EnrollmentService enrollmentService, ReportService reportService, Runnable onLogout) {
        this.parentFrame = parentFrame;
        this.userService = userService;
        this.courseService = courseService;
        this.enrollmentService = enrollmentService;
        this.reportService = reportService;
        this.onLogout = onLogout;

        setLayout(new BorderLayout());
        setBackground(UITheme.BG_LIGHT);

        initUI();
    }

    public void setCurrentInstructor(Instructor instructor) {
        this.currentInstructor = instructor;
        refreshHeader();
        refreshAllData();
    }

    private void refreshHeader() {
        headerContainer.removeAll();
        if (currentInstructor != null) {
            JPanel banner = UITheme.createHeaderBanner(
                    "EduTrack | Faculty Portal",
                    "Department of " + currentInstructor.getDepartment() + " • " + currentInstructor.getSpecialization(),
                    "Faculty Instructor",
                    currentInstructor.getName(),
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

        tabbedPane.addTab("📖 My Assigned Courses", createMyCoursesPanel());
        tabbedPane.addTab("👥 Class Roster & Grading", createRosterPanel());
        tabbedPane.addTab("📈 Course Analytics", createAnalyticsPanel());
        tabbedPane.addTab("👤 Faculty Profile", createProfilePanel());

        add(tabbedPane, BorderLayout.CENTER);
    }

    // ==========================================
    // TAB 1: ASSIGNED COURSES
    // ==========================================
    private JPanel createMyCoursesPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(UITheme.BG_LIGHT);
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        toolbar.setOpaque(false);

        JButton btnRefresh = UITheme.createSecondaryButton("🔄 Refresh Courses");
        btnRefresh.addActionListener(e -> loadMyCourses());
        toolbar.add(btnRefresh);

        panel.add(toolbar, BorderLayout.NORTH);

        String[] cols = {"Course Code", "Course Title", "Credits", "Department", "Active Students", "Max Capacity", "Prerequisites"};
        modelMyCourses = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        tblMyCourses = new JTable(modelMyCourses);
        UITheme.styleTable(tblMyCourses);

        panel.add(new JScrollPane(tblMyCourses), BorderLayout.CENTER);
        return panel;
    }

    private void loadMyCourses() {
        if (currentInstructor == null) return;
        modelMyCourses.setRowCount(0);
        List<Course> courses = courseService.getCoursesByInstructor(currentInstructor.getId());
        for (Course c : courses) {
            int active = courseService.getActiveEnrollmentCount(c.getCourseCode());
            String prereqs = c.getPrerequisites().isEmpty() ? "None" : String.join(", ", c.getPrerequisites());
            modelMyCourses.addRow(new Object[]{
                    c.getCourseCode(),
                    c.getTitle(),
                    c.getCredits(),
                    c.getDepartment(),
                    active,
                    c.getMaxCapacity(),
                    prereqs
            });
        }
    }

    // ==========================================
    // TAB 2: CLASS ROSTER & GRADING
    // ==========================================
    private JPanel createRosterPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(UITheme.BG_LIGHT);
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        // Top Filter Bar
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        topBar.setOpaque(false);

        JLabel lblCourse = new JLabel("Select Course Class:");
        lblCourse.setFont(UITheme.FONT_BOLD);

        cmbRosterCourses = new JComboBox<>();
        cmbRosterCourses.setFont(UITheme.FONT_BODY);
        cmbRosterCourses.addActionListener(e -> loadRosterData());

        JButton btnRefresh = UITheme.createSecondaryButton("🔄 Refresh");
        btnRefresh.addActionListener(e -> loadRosterData());

        topBar.add(lblCourse);
        topBar.add(cmbRosterCourses);
        topBar.add(btnRefresh);
        panel.add(topBar, BorderLayout.NORTH);

        // Table
        String[] cols = {"Enrollment ID", "Student ID", "Student Name", "Semester", "Status", "Score / Marks", "Letter Grade"};
        modelRoster = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        tblRoster = new JTable(modelRoster);
        UITheme.styleTable(tblRoster);

        tblRoster.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateSelectedStudent();
            }
        });

        panel.add(new JScrollPane(tblRoster), BorderLayout.CENTER);

        // Bottom Grading Toolbar Card
        JPanel cardGrading = UITheme.createCardPanel();
        cardGrading.setLayout(new FlowLayout(FlowLayout.LEFT, 16, 6));

        lblSelectedStudent = new JLabel("Select a student row above to record marks");
        lblSelectedStudent.setFont(UITheme.FONT_BOLD);
        lblSelectedStudent.setForeground(UITheme.TEXT_BODY);

        JLabel lblScore = new JLabel("Enter Score (0-100):");
        lblScore.setFont(UITheme.FONT_BODY);

        txtScoreInput = new JTextField(6);
        txtScoreInput.setFont(UITheme.FONT_BODY);
        txtScoreInput.setBorder(new CompoundBorder(
                new LineBorder(UITheme.BORDER_DARK, 1),
                new EmptyBorder(6, 8, 6, 8)
        ));

        btnSubmitGrade = UITheme.createPrimaryButton("💾 Save Grade");
        btnSubmitGrade.setEnabled(false);
        btnSubmitGrade.addActionListener(e -> handleSubmitGrade());

        cardGrading.add(lblSelectedStudent);
        cardGrading.add(lblScore);
        cardGrading.add(txtScoreInput);
        cardGrading.add(btnSubmitGrade);

        panel.add(cardGrading, BorderLayout.SOUTH);
        return panel;
    }

    private void updateSelectedStudent() {
        int row = tblRoster.getSelectedRow();
        if (row >= 0) {
            String enrId = modelRoster.getValueAt(row, 0).toString();
            String stuId = modelRoster.getValueAt(row, 1).toString();
            String stuName = modelRoster.getValueAt(row, 2).toString();
            String currentScore = modelRoster.getValueAt(row, 5).toString();

            lblSelectedStudent.setText("Grading: " + stuName + " (" + stuId + ") [" + enrId + "]");
            txtScoreInput.setText(currentScore.equalsIgnoreCase("Pending") ? "" : currentScore);
            btnSubmitGrade.setEnabled(true);
        } else {
            lblSelectedStudent.setText("Select a student row above to record marks");
            txtScoreInput.setText("");
            btnSubmitGrade.setEnabled(false);
        }
    }

    private void loadRosterData() {
        if (currentInstructor == null || cmbRosterCourses.getSelectedIndex() < 0) return;
        List<Course> courses = courseService.getCoursesByInstructor(currentInstructor.getId());
        if (courses.isEmpty() || cmbRosterCourses.getSelectedIndex() >= courses.size()) return;

        Course course = courses.get(cmbRosterCourses.getSelectedIndex());
        modelRoster.setRowCount(0);

        List<Enrollment> list = enrollmentService.getCourseEnrollments(course.getCourseCode());
        for (Enrollment e : list) {
            String stuName = userService.getAllUsers().stream()
                    .filter(u -> u.getId().equalsIgnoreCase(e.getStudentId()))
                    .findFirst()
                    .map(User::getName)
                    .orElse("Unknown");

            String scoreStr = e.getScore() >= 0 ? String.format("%.1f", e.getScore()) : "Pending";
            modelRoster.addRow(new Object[]{
                    e.getEnrollmentId(),
                    e.getStudentId(),
                    stuName,
                    e.getSemester(),
                    e.getStatus().name(),
                    scoreStr,
                    e.getGrade().getLetter()
            });
        }
        updateSelectedStudent();
    }

    private void handleSubmitGrade() {
        int row = tblRoster.getSelectedRow();
        if (row < 0) return;

        String enrId = modelRoster.getValueAt(row, 0).toString();
        String input = txtScoreInput.getText().trim();

        if (input.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a numeric score between 0.0 and 100.0", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            double score = Double.parseDouble(input);
            if (score < 0.0 || score > 100.0) {
                JOptionPane.showMessageDialog(this, "Score must be between 0.0 and 100.0.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }

            enrollmentService.submitGrade(enrId, score, currentInstructor.getId());
            Grade assignedGrade = Grade.fromScore(score);

            JOptionPane.showMessageDialog(this,
                    String.format("Grade recorded successfully!\nScore: %.1f -> Grade: %s (%s)",
                            score, assignedGrade.getLetter(), assignedGrade.getDescription()),
                    "Success", JOptionPane.INFORMATION_MESSAGE);

            loadRosterData();
            loadAnalyticsData();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid number format. Please enter a valid decimal score.", "Format Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ==========================================
    // TAB 3: ANALYTICS
    // ==========================================
    private JPanel createAnalyticsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setBackground(UITheme.BG_LIGHT);
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        top.setOpaque(false);

        JLabel lbl = new JLabel("Course Analytics:");
        lbl.setFont(UITheme.FONT_BOLD);
        cmbAnalyticsCourse = new JComboBox<>();
        cmbAnalyticsCourse.setFont(UITheme.FONT_BODY);
        cmbAnalyticsCourse.addActionListener(e -> updateAnalytics());

        top.add(lbl);
        top.add(cmbAnalyticsCourse);
        panel.add(top, BorderLayout.NORTH);

        JPanel centerCard = UITheme.createCardPanel();
        centerCard.setLayout(new BorderLayout(0, 12));

        lblAnalyticsSummary = new JLabel("Select a course to view academic breakdown");
        lblAnalyticsSummary.setFont(UITheme.FONT_SUBHEADER);
        centerCard.add(lblAnalyticsSummary, BorderLayout.NORTH);

        String[] cols = {"Grade", "Grade Points", "Total Count", "Distribution Bar"};
        modelGradeBreakdown = new DefaultTableModel(cols, 0);
        tblGradeBreakdown = new JTable(modelGradeBreakdown);
        UITheme.styleTable(tblGradeBreakdown);

        centerCard.add(new JScrollPane(tblGradeBreakdown), BorderLayout.CENTER);
        panel.add(centerCard, BorderLayout.CENTER);

        return panel;
    }

    private void loadAnalyticsData() {
        if (currentInstructor == null) return;
        cmbAnalyticsCourse.removeAllItems();
        List<Course> courses = courseService.getCoursesByInstructor(currentInstructor.getId());
        for (Course c : courses) {
            cmbAnalyticsCourse.addItem(c.getCourseCode() + " - " + c.getTitle());
        }
        if (!courses.isEmpty()) {
            cmbAnalyticsCourse.setSelectedIndex(0);
        }
    }

    private void updateAnalytics() {
        if (currentInstructor == null || cmbAnalyticsCourse.getSelectedIndex() < 0) return;
        List<Course> courses = courseService.getCoursesByInstructor(currentInstructor.getId());
        if (courses.isEmpty() || cmbAnalyticsCourse.getSelectedIndex() >= courses.size()) return;

        Course course = courses.get(cmbAnalyticsCourse.getSelectedIndex());
        try {
            ReportService.CoursePerformance perf = reportService.getCoursePerformance(course.getCourseCode());
            lblAnalyticsSummary.setText(String.format("<html><b>Enrolled:</b> %d &nbsp;|&nbsp; <b>Graded:</b> %d &nbsp;|&nbsp; " +
                            "<b>Average Score:</b> <span style='color: #2563EB;'>%.2f / 100</span> &nbsp;|&nbsp; <b>Pass Rate:</b> %.1f%%</html>",
                    perf.totalEnrolled, perf.completedCount, perf.averageScore, perf.passRate));

            modelGradeBreakdown.setRowCount(0);
            for (Map.Entry<Grade, Integer> entry : perf.gradeDistribution.entrySet()) {
                Grade g = entry.getKey();
                int cnt = entry.getValue();
                String bar = "█".repeat(Math.max(1, cnt * 4));
                modelGradeBreakdown.addRow(new Object[]{
                        g.getLetter() + " (" + g.getDescription() + ")",
                        String.format("%.1f", g.getGradePoint()),
                        cnt + " student(s)",
                        bar
                });
            }
        } catch (Exception ex) {
            // ignore
        }
    }

    // ==========================================
    // TAB 4: FACULTY PROFILE
    // ==========================================
    private JPanel createProfilePanel() {
        JPanel container = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 30));
        container.setBackground(UITheme.BG_LIGHT);

        pnlProfile = UITheme.createCardPanel();
        pnlProfile.setPreferredSize(new Dimension(520, 320));
        pnlProfile.setLayout(new GridLayout(6, 1, 0, 8));

        container.add(pnlProfile);
        return container;
    }

    private void refreshProfile() {
        pnlProfile.removeAll();
        if (currentInstructor != null) {
            JLabel lblHdr = new JLabel("👨‍🏫 Faculty Member Profile");
            lblHdr.setFont(UITheme.FONT_TITLE);
            lblHdr.setForeground(UITheme.PRIMARY);

            JLabel l1 = new JLabel("Faculty ID:           " + currentInstructor.getId());
            JLabel l2 = new JLabel("Full Legal Name:      " + currentInstructor.getName());
            JLabel l3 = new JLabel("University Email:     " + currentInstructor.getEmail());
            JLabel l4 = new JLabel("Academic Department:  " + currentInstructor.getDepartment());
            JLabel l5 = new JLabel("Specialization:       " + currentInstructor.getSpecialization());

            l1.setFont(UITheme.FONT_BODY);
            l2.setFont(UITheme.FONT_BODY);
            l3.setFont(UITheme.FONT_BODY);
            l4.setFont(UITheme.FONT_BODY);
            l5.setFont(UITheme.FONT_BODY);

            pnlProfile.add(lblHdr);
            pnlProfile.add(l1);
            pnlProfile.add(l2);
            pnlProfile.add(l3);
            pnlProfile.add(l4);
            pnlProfile.add(l5);
        }
        pnlProfile.revalidate();
        pnlProfile.repaint();
    }

    public void refreshAllData() {
        loadMyCourses();

        // Refresh dropdowns
        if (currentInstructor != null) {
            cmbRosterCourses.removeAllItems();
            List<Course> courses = courseService.getCoursesByInstructor(currentInstructor.getId());
            for (Course c : courses) {
                cmbRosterCourses.addItem(c.getCourseCode() + " - " + c.getTitle());
            }
            if (!courses.isEmpty()) {
                cmbRosterCourses.setSelectedIndex(0);
            }
        }

        loadRosterData();
        loadAnalyticsData();
        refreshProfile();
    }
}

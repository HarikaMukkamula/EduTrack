package com.edutrack.gui;

import com.edutrack.exception.RecordNotFoundException;
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
import java.util.Optional;

/**
 * AdminDashboardPanel: Complete administrative control center for course creation,
 * faculty assignment, prerequisite management, student records, and system analytics.
 */
public class AdminDashboardPanel extends JPanel {
    private final Frame parentFrame;
    private final UserService userService;
    private final CourseService courseService;
    private final EnrollmentService enrollmentService;
    private final ReportService reportService;
    private final Runnable onLogout;

    private Admin currentAdmin;

    // GUI Components
    private JPanel headerContainer;
    private JTabbedPane tabbedPane;

    // Courses Tab
    private JTable tblCourses;
    private DefaultTableModel modelCourses;

    // Students Tab
    private JTable tblStudents;
    private DefaultTableModel modelStudents;

    // Instructors Tab
    private JTable tblInstructors;
    private DefaultTableModel modelInstructors;

    // Reports Tab
    private JPanel pnlStatsCards;
    private JComboBox<String> cmbAnalyticsCourse;
    private JLabel lblPerfEnrolled;
    private JLabel lblPerfAvgScore;
    private JLabel lblPerfPassRate;
    private JTable tblGradeDist;
    private DefaultTableModel modelGradeDist;

    public AdminDashboardPanel(Frame parentFrame, UserService userService, CourseService courseService,
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

    public void setCurrentAdmin(Admin admin) {
        this.currentAdmin = admin;
        refreshHeader();
        refreshAllData();
    }

    private void refreshHeader() {
        headerContainer.removeAll();
        if (currentAdmin != null) {
            JPanel banner = UITheme.createHeaderBanner(
                    "EduTrack | Administrator Dashboard",
                    "GCET Academic Registrar Portal • " + currentAdmin.getDepartment(),
                    currentAdmin.getAdminLevel(),
                    currentAdmin.getName(),
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

        tabbedPane.addTab("📚 Course Management", createCoursesPanel());
        tabbedPane.addTab("🎓 Student Directory", createStudentsPanel());
        tabbedPane.addTab("👨‍🏫 Faculty Instructors", createInstructorsPanel());
        tabbedPane.addTab("📊 University Analytics & Reports", createAnalyticsPanel());

        add(tabbedPane, BorderLayout.CENTER);
    }

    // ==========================================
    // TAB 1: COURSE MANAGEMENT
    // ==========================================
    private JPanel createCoursesPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(UITheme.BG_LIGHT);
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        // Action Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        toolbar.setOpaque(false);

        JButton btnAddCourse = UITheme.createPrimaryButton("➕ Add New Course");
        btnAddCourse.addActionListener(e -> showAddCourseDialog());

        JButton btnAssignInstructor = UITheme.createSecondaryButton("👨‍🏫 Assign Instructor");
        btnAssignInstructor.addActionListener(e -> showAssignInstructorDialog());

        JButton btnAddPrereq = UITheme.createSecondaryButton("🔗 Add Prerequisite");
        btnAddPrereq.addActionListener(e -> showAddPrerequisiteDialog());

        JButton btnDeleteCourse = UITheme.createDangerButton("🗑️ Delete Course");
        btnDeleteCourse.addActionListener(e -> handleDeleteCourse());

        JButton btnRefresh = UITheme.createSecondaryButton("🔄 Refresh");
        btnRefresh.addActionListener(e -> loadCoursesData());

        toolbar.add(btnAddCourse);
        toolbar.add(btnAssignInstructor);
        toolbar.add(btnAddPrereq);
        toolbar.add(btnDeleteCourse);
        toolbar.add(btnRefresh);

        panel.add(toolbar, BorderLayout.NORTH);

        // Table
        String[] columns = {"Course Code", "Title", "Credits", "Department", "Instructor", "Seats (Filled/Max)", "Prerequisites"};
        modelCourses = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblCourses = new JTable(modelCourses);
        UITheme.styleTable(tblCourses);

        JScrollPane scrollPane = new JScrollPane(tblCourses);
        scrollPane.setBorder(new LineBorder(UITheme.BORDER, 1));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void loadCoursesData() {
        modelCourses.setRowCount(0);
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

            modelCourses.addRow(new Object[]{
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

    private void showAddCourseDialog() {
        JDialog dlg = new JDialog(parentFrame, "Add New University Course", true);
        dlg.setSize(440, 420);
        dlg.setLocationRelativeTo(parentFrame);
        dlg.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(16, 20, 16, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 4, 6, 4);

        JTextField txtCode = new JTextField("CSE");
        JTextField txtTitle = new JTextField();
        JTextField txtDesc = new JTextField();
        JSpinner spnCredits = new JSpinner(new SpinnerNumberModel(4, 1, 6, 1));
        JTextField txtDept = new JTextField("CSE");
        JSpinner spnCap = new JSpinner(new SpinnerNumberModel(40, 5, 200, 5));

        // Instructor Dropdown
        List<User> instructors = userService.getUsersByRole(Role.INSTRUCTOR);
        JComboBox<String> cmbInstructor = new JComboBox<>();
        cmbInstructor.addItem("None (Unassigned)");
        for (User u : instructors) {
            cmbInstructor.addItem(u.getId() + " - " + u.getName());
        }

        addDialogRow(form, gbc, 0, "Course Code (e.g., CSE402):", txtCode);
        addDialogRow(form, gbc, 1, "Course Title:", txtTitle);
        addDialogRow(form, gbc, 2, "Description:", txtDesc);
        addDialogRow(form, gbc, 3, "Credits (1-6):", spnCredits);
        addDialogRow(form, gbc, 4, "Department:", txtDept);
        addDialogRow(form, gbc, 5, "Max Capacity:", spnCap);
        addDialogRow(form, gbc, 6, "Assigned Instructor:", cmbInstructor);

        dlg.add(form, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton btnCancel = UITheme.createSecondaryButton("Cancel");
        btnCancel.addActionListener(e -> dlg.dispose());

        JButton btnSave = UITheme.createPrimaryButton("Create Course");
        btnSave.addActionListener(e -> {
            String code = txtCode.getText().trim().toUpperCase();
            String title = txtTitle.getText().trim();
            String desc = txtDesc.getText().trim();
            int credits = (Integer) spnCredits.getValue();
            String dept = txtDept.getText().trim();
            int cap = (Integer) spnCap.getValue();

            String insId = null;
            if (cmbInstructor.getSelectedIndex() > 0) {
                insId = instructors.get(cmbInstructor.getSelectedIndex() - 1).getId();
            }

            if (code.isEmpty() || title.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Course Code and Title cannot be empty.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                courseService.addCourse(code, title, desc, credits, dept, cap, insId);
                JOptionPane.showMessageDialog(dlg, "Course " + code + " created successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dlg.dispose();
                loadCoursesData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);
        dlg.add(btnPanel, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private void showAssignInstructorDialog() {
        int selectedRow = tblCourses.getSelectedRow();
        String defaultCode = selectedRow >= 0 ? modelCourses.getValueAt(selectedRow, 0).toString() : null;

        List<Course> courses = courseService.getAllCourses();
        List<User> instructors = userService.getUsersByRole(Role.INSTRUCTOR);

        if (courses.isEmpty() || instructors.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Courses and Instructors must exist first.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JComboBox<String> cmbCourses = new JComboBox<>();
        for (Course c : courses) {
            cmbCourses.addItem(c.getCourseCode() + " - " + c.getTitle());
            if (defaultCode != null && c.getCourseCode().equalsIgnoreCase(defaultCode)) {
                cmbCourses.setSelectedIndex(cmbCourses.getItemCount() - 1);
            }
        }

        JComboBox<String> cmbIns = new JComboBox<>();
        for (User u : instructors) {
            cmbIns.addItem(u.getId() + " - " + u.getName() + " (" + u.getAffiliation() + ")");
        }

        JPanel pnl = new JPanel(new GridLayout(2, 2, 8, 12));
        pnl.add(new JLabel("Select Course:"));
        pnl.add(cmbCourses);
        pnl.add(new JLabel("Select Instructor:"));
        pnl.add(cmbIns);

        int res = JOptionPane.showConfirmDialog(this, pnl, "Assign Instructor to Course", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res == JOptionPane.OK_OPTION) {
            String code = courses.get(cmbCourses.getSelectedIndex()).getCourseCode();
            String insId = instructors.get(cmbIns.getSelectedIndex()).getId();
            try {
                courseService.assignInstructor(code, insId);
                JOptionPane.showMessageDialog(this, "Instructor assigned successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadCoursesData();
                loadInstructorsData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showAddPrerequisiteDialog() {
        List<Course> courses = courseService.getAllCourses();
        if (courses.size() < 2) {
            JOptionPane.showMessageDialog(this, "At least two courses must exist to link prerequisites.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JComboBox<String> cmbTarget = new JComboBox<>();
        JComboBox<String> cmbPrereq = new JComboBox<>();

        for (Course c : courses) {
            String item = c.getCourseCode() + " - " + c.getTitle();
            cmbTarget.addItem(item);
            cmbPrereq.addItem(item);
        }

        JPanel pnl = new JPanel(new GridLayout(2, 2, 8, 12));
        pnl.add(new JLabel("Target Advanced Course:"));
        pnl.add(cmbTarget);
        pnl.add(new JLabel("Required Prerequisite:"));
        pnl.add(cmbPrereq);

        int res = JOptionPane.showConfirmDialog(this, pnl, "Link Course Prerequisite", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res == JOptionPane.OK_OPTION) {
            String target = courses.get(cmbTarget.getSelectedIndex()).getCourseCode();
            String prereq = courses.get(cmbPrereq.getSelectedIndex()).getCourseCode();

            try {
                courseService.addPrerequisite(target, prereq);
                JOptionPane.showMessageDialog(this, "Prerequisite " + prereq + " added to " + target + "!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadCoursesData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleDeleteCourse() {
        int selected = tblCourses.getSelectedRow();
        if (selected < 0) {
            JOptionPane.showMessageDialog(this, "Please select a course to delete from the table.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String code = modelCourses.getValueAt(selected, 0).toString();
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete course " + code + "?\nThis will also remove it from any faculty assignments.",
                "Confirm Course Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                courseService.deleteCourse(code);
                JOptionPane.showMessageDialog(this, "Course " + code + " deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadCoursesData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ==========================================
    // TAB 2: STUDENT DIRECTORY
    // ==========================================
    private JPanel createStudentsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(UITheme.BG_LIGHT);
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        toolbar.setOpaque(false);

        JButton btnTranscript = UITheme.createPrimaryButton("📄 View Academic Transcript");
        btnTranscript.addActionListener(e -> showStudentTranscriptDialog());

        JButton btnDeleteStudent = UITheme.createDangerButton("🗑️ Remove Student");
        btnDeleteStudent.addActionListener(e -> handleDeleteStudent());

        JButton btnRefresh = UITheme.createSecondaryButton("🔄 Refresh");
        btnRefresh.addActionListener(e -> loadStudentsData());

        toolbar.add(btnTranscript);
        toolbar.add(btnDeleteStudent);
        toolbar.add(btnRefresh);

        panel.add(toolbar, BorderLayout.NORTH);

        String[] cols = {"Student ID", "Full Name", "University Email", "Major", "Year", "Sem", "CGPA", "Phone"};
        modelStudents = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblStudents = new JTable(modelStudents);
        UITheme.styleTable(tblStudents);

        JScrollPane scroll = new JScrollPane(tblStudents);
        scroll.setBorder(new LineBorder(UITheme.BORDER, 1));
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private void loadStudentsData() {
        modelStudents.setRowCount(0);
        List<User> students = userService.getUsersByRole(Role.STUDENT);
        for (User u : students) {
            Student s = (Student) u;
            double gpa = enrollmentService.calculateGPA(s.getId());
            modelStudents.addRow(new Object[]{
                    s.getId(),
                    s.getName(),
                    s.getEmail(),
                    s.getMajor(),
                    "Year " + s.getYearOfStudy(),
                    "Sem " + s.getCurrentSemester(),
                    String.format("%.2f / 4.0", gpa),
                    s.getContactNumber()
            });
        }
    }

    private void showStudentTranscriptDialog() {
        int selected = tblStudents.getSelectedRow();
        if (selected < 0) {
            JOptionPane.showMessageDialog(this, "Please select a student from the table first.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String studentId = modelStudents.getValueAt(selected, 0).toString();
        try {
            Student student = (Student) userService.getUserById(studentId);

            JDialog dlg = new JDialog(parentFrame, "Official Academic Transcript - " + student.getName(), true);
            dlg.setSize(680, 480);
            dlg.setLocationRelativeTo(parentFrame);
            dlg.setLayout(new BorderLayout());

            // Header Info
            JPanel hdr = new JPanel(new GridLayout(4, 1, 0, 4));
            hdr.setBackground(UITheme.PRIMARY);
            hdr.setBorder(new EmptyBorder(14, 20, 14, 20));

            JLabel l1 = new JLabel("🎓 GEETHANJALI COLLEGE OF ENGINEERING & TECHNOLOGY");
            l1.setFont(UITheme.FONT_HEADER);
            l1.setForeground(Color.WHITE);

            JLabel l2 = new JLabel("Official Student Academic Transcript");
            l2.setFont(UITheme.FONT_SUBHEADER);
            l2.setForeground(new Color(224, 231, 255));

            JLabel l3 = new JLabel("Student: " + student.getName() + " (" + student.getId() + ") | Email: " + student.getEmail());
            l3.setFont(UITheme.FONT_BODY);
            l3.setForeground(Color.WHITE);

            JLabel l4 = new JLabel("Program: " + student.getMajor() + " • Year " + student.getYearOfStudy() + " (Sem " + student.getCurrentSemester() + ")");
            l4.setFont(UITheme.FONT_SMALL);
            l4.setForeground(new Color(203, 213, 225));

            hdr.add(l1);
            hdr.add(l2);
            hdr.add(l3);
            hdr.add(l4);
            dlg.add(hdr, BorderLayout.NORTH);

            // Table
            String[] cols = {"Course Code", "Course Title", "Credits", "Semester", "Status", "Score", "Grade", "Points"};
            DefaultTableModel transcriptModel = new DefaultTableModel(cols, 0);
            JTable tblTranscript = new JTable(transcriptModel);
            UITheme.styleTable(tblTranscript);

            List<Enrollment> enrollments = enrollmentService.getStudentEnrollments(student.getId());
            int totalEarnedCredits = 0;

            for (Enrollment e : enrollments) {
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

                transcriptModel.addRow(new Object[]{
                        e.getCourseCode(), title, credits, e.getSemester(),
                        e.getStatus().name(), scoreStr, e.getGrade().getLetter(), pointsStr
                });
            }

            dlg.add(new JScrollPane(tblTranscript), BorderLayout.CENTER);

            // Footer GPA Card
            double gpa = enrollmentService.calculateGPA(student.getId());
            String standing = gpa >= 3.5 ? "Dean's Honor List 🏆" : (gpa >= 2.0 ? "Good Academic Standing" : "Academic Probation Warning");

            JPanel footer = new JPanel(new BorderLayout());
            footer.setBackground(Color.WHITE);
            footer.setBorder(new EmptyBorder(12, 20, 12, 20));

            JLabel lblGpa = new JLabel("<html><b>Earned Credits:</b> " + totalEarnedCredits + " &nbsp;|&nbsp; " +
                    "<b>Cumulative GPA:</b> <span style='color: #2563EB; font-size: 14px;'>" + String.format("%.2f", gpa) + " / 4.00</span> &nbsp;|&nbsp; " +
                    "<b>Standing:</b> " + standing + "</html>");
            lblGpa.setFont(UITheme.FONT_BODY);

            JButton btnClose = UITheme.createPrimaryButton("Close");
            btnClose.addActionListener(ev -> dlg.dispose());

            footer.add(lblGpa, BorderLayout.WEST);
            footer.add(btnClose, BorderLayout.EAST);
            dlg.add(footer, BorderLayout.SOUTH);

            dlg.setVisible(true);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleDeleteStudent() {
        int selected = tblStudents.getSelectedRow();
        if (selected < 0) {
            JOptionPane.showMessageDialog(this, "Please select a student to remove.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String studentId = modelStudents.getValueAt(selected, 0).toString();
        String name = modelStudents.getValueAt(selected, 1).toString();

        int confirm = JOptionPane.showConfirmDialog(this,
                "Remove student " + name + " (" + studentId + ")?",
                "Confirm Removal", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                userService.removeUser(studentId);
                loadStudentsData();
                JOptionPane.showMessageDialog(this, "Student removed successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ==========================================
    // TAB 3: INSTRUCTOR DIRECTORY
    // ==========================================
    private JPanel createInstructorsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(UITheme.BG_LIGHT);
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        toolbar.setOpaque(false);

        JButton btnAddInstructor = UITheme.createPrimaryButton("➕ Register New Faculty");
        btnAddInstructor.addActionListener(e -> showAddInstructorDialog());

        JButton btnDelete = UITheme.createDangerButton("🗑️ Remove Faculty");
        btnDelete.addActionListener(e -> handleDeleteInstructor());

        JButton btnRefresh = UITheme.createSecondaryButton("🔄 Refresh");
        btnRefresh.addActionListener(e -> loadInstructorsData());

        toolbar.add(btnAddInstructor);
        toolbar.add(btnDelete);
        toolbar.add(btnRefresh);

        panel.add(toolbar, BorderLayout.NORTH);

        String[] cols = {"Instructor ID", "Faculty Name", "Email", "Department", "Specialization", "Assigned Courses"};
        modelInstructors = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblInstructors = new JTable(modelInstructors);
        UITheme.styleTable(tblInstructors);

        JScrollPane scroll = new JScrollPane(tblInstructors);
        scroll.setBorder(new LineBorder(UITheme.BORDER, 1));
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private void loadInstructorsData() {
        modelInstructors.setRowCount(0);
        List<User> instructors = userService.getUsersByRole(Role.INSTRUCTOR);
        for (User u : instructors) {
            Instructor ins = (Instructor) u;
            modelInstructors.addRow(new Object[]{
                    ins.getId(),
                    ins.getName(),
                    ins.getEmail(),
                    ins.getDepartment(),
                    ins.getSpecialization(),
                    String.join(", ", ins.getAssignedCourseCodes())
            });
        }
    }

    private void showAddInstructorDialog() {
        JDialog dlg = new JDialog(parentFrame, "Register New Faculty Instructor", true);
        dlg.setSize(440, 360);
        dlg.setLocationRelativeTo(parentFrame);
        dlg.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(16, 20, 16, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 4, 6, 4);

        JTextField txtName = new JTextField();
        JTextField txtEmail = new JTextField("@gcet.edu.in");
        JPasswordField txtPwd = new JPasswordField("prof123");
        JTextField txtDept = new JTextField("CSE");
        JTextField txtSpec = new JTextField();

        addDialogRow(form, gbc, 0, "Full Legal Name *:", txtName);
        addDialogRow(form, gbc, 1, "University Email *:", txtEmail);
        addDialogRow(form, gbc, 2, "Password *:", txtPwd);
        addDialogRow(form, gbc, 3, "Department *:", txtDept);
        addDialogRow(form, gbc, 4, "Specialization:", txtSpec);

        dlg.add(form, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton btnCancel = UITheme.createSecondaryButton("Cancel");
        btnCancel.addActionListener(e -> dlg.dispose());

        JButton btnSave = UITheme.createPrimaryButton("Create Account");
        btnSave.addActionListener(e -> {
            String name = txtName.getText().trim();
            String email = txtEmail.getText().trim();
            String pwd = new String(txtPwd.getPassword()).trim();
            String dept = txtDept.getText().trim();
            String spec = txtSpec.getText().trim();

            if (name.isEmpty() || email.isEmpty() || pwd.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Please fill in all required fields.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                Instructor created = userService.registerInstructor(name, email, pwd, dept, spec);
                JOptionPane.showMessageDialog(dlg, "Instructor registered!\nAssigned ID: " + created.getId(), "Success", JOptionPane.INFORMATION_MESSAGE);
                dlg.dispose();
                loadInstructorsData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);
        dlg.add(btnPanel, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private void handleDeleteInstructor() {
        int selected = tblInstructors.getSelectedRow();
        if (selected < 0) {
            JOptionPane.showMessageDialog(this, "Please select an instructor to remove.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String insId = modelInstructors.getValueAt(selected, 0).toString();
        String name = modelInstructors.getValueAt(selected, 1).toString();

        int confirm = JOptionPane.showConfirmDialog(this,
                "Remove faculty member " + name + " (" + insId + ")?",
                "Confirm Removal", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                userService.removeUser(insId);
                loadInstructorsData();
                loadCoursesData();
                JOptionPane.showMessageDialog(this, "Faculty member removed successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ==========================================
    // TAB 4: ANALYTICS & REPORTS
    // ==========================================
    private JPanel createAnalyticsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setBackground(UITheme.BG_LIGHT);
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        // Top Metrics Grid
        pnlStatsCards = new JPanel(new GridLayout(1, 4, 12, 0));
        pnlStatsCards.setOpaque(false);
        panel.add(pnlStatsCards, BorderLayout.NORTH);

        // Center: Course Performance Inspector Card
        JPanel cardPerf = UITheme.createCardPanel();
        cardPerf.setLayout(new BorderLayout(0, 12));

        JPanel perfHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        perfHeader.setOpaque(false);

        JLabel lblSelect = new JLabel("Inspect Course Analytics:");
        lblSelect.setFont(UITheme.FONT_BOLD);
        cmbAnalyticsCourse = new JComboBox<>();
        cmbAnalyticsCourse.setFont(UITheme.FONT_BODY);
        cmbAnalyticsCourse.addActionListener(e -> updateCourseAnalytics());

        perfHeader.add(lblSelect);
        perfHeader.add(cmbAnalyticsCourse);
        cardPerf.add(perfHeader, BorderLayout.NORTH);

        // Stats summary row
        JPanel perfStatsRow = new JPanel(new GridLayout(1, 3, 12, 0));
        perfStatsRow.setOpaque(false);

        lblPerfEnrolled = new JLabel("Total Enrolled: -");
        lblPerfEnrolled.setFont(UITheme.FONT_SUBHEADER);

        lblPerfAvgScore = new JLabel("Average Score: -");
        lblPerfAvgScore.setFont(UITheme.FONT_SUBHEADER);

        lblPerfPassRate = new JLabel("Pass Rate: -");
        lblPerfPassRate.setFont(UITheme.FONT_SUBHEADER);

        perfStatsRow.add(lblPerfEnrolled);
        perfStatsRow.add(lblPerfAvgScore);
        perfStatsRow.add(lblPerfPassRate);

        // Grade Distribution Table
        String[] cols = {"Letter Grade", "Grade Points", "Number of Students", "Visual Distribution Bar"};
        modelGradeDist = new DefaultTableModel(cols, 0);
        tblGradeDist = new JTable(modelGradeDist);
        UITheme.styleTable(tblGradeDist);

        JPanel centerPerf = new JPanel(new BorderLayout(0, 8));
        centerPerf.setOpaque(false);
        centerPerf.add(perfStatsRow, BorderLayout.NORTH);
        centerPerf.add(new JScrollPane(tblGradeDist), BorderLayout.CENTER);

        cardPerf.add(centerPerf, BorderLayout.CENTER);
        panel.add(cardPerf, BorderLayout.CENTER);

        return panel;
    }

    private void loadAnalyticsData() {
        // Refresh Top KPI Cards
        pnlStatsCards.removeAll();
        ReportService.SystemStats stats = reportService.getSystemStats();

        pnlStatsCards.add(UITheme.createMetricCard("Total Users", String.valueOf(stats.totalUsers), UITheme.PRIMARY));
        pnlStatsCards.add(UITheme.createMetricCard("University Courses", String.valueOf(stats.totalCourses), UITheme.ACCENT));
        pnlStatsCards.add(UITheme.createMetricCard("Active Enrollments", String.valueOf(stats.activeEnrollments), UITheme.WARNING));
        pnlStatsCards.add(UITheme.createMetricCard("Completed Credits", String.valueOf(stats.completedEnrollments), UITheme.SUCCESS));

        pnlStatsCards.revalidate();
        pnlStatsCards.repaint();

        // Refresh Dropdown
        cmbAnalyticsCourse.removeAllItems();
        List<Course> courses = courseService.getAllCourses();
        for (Course c : courses) {
            cmbAnalyticsCourse.addItem(c.getCourseCode() + " - " + c.getTitle());
        }

        if (!courses.isEmpty()) {
            cmbAnalyticsCourse.setSelectedIndex(0);
        }
    }

    private void updateCourseAnalytics() {
        if (cmbAnalyticsCourse.getSelectedIndex() < 0) return;
        List<Course> courses = courseService.getAllCourses();
        if (courses.isEmpty()) return;

        Course course = courses.get(cmbAnalyticsCourse.getSelectedIndex());
        try {
            ReportService.CoursePerformance perf = reportService.getCoursePerformance(course.getCourseCode());
            lblPerfEnrolled.setText("Total Enrolled: " + perf.totalEnrolled + " (" + perf.completedCount + " graded)");
            lblPerfAvgScore.setText(String.format("Average Score: %.2f / 100", perf.averageScore));
            lblPerfPassRate.setText(String.format("Pass Rate: %.1f%%", perf.passRate));

            modelGradeDist.setRowCount(0);
            for (Map.Entry<Grade, Integer> entry : perf.gradeDistribution.entrySet()) {
                Grade g = entry.getKey();
                int count = entry.getValue();
                String bar = "█".repeat(Math.max(1, count * 3));
                modelGradeDist.addRow(new Object[]{
                        g.getLetter() + " (" + g.getDescription() + ")",
                        String.format("%.1f pts", g.getGradePoint()),
                        count + " student(s)",
                        bar
                });
            }
        } catch (Exception ex) {
            // ignore
        }
    }

    // ==========================================
    // HELPERS
    // ==========================================
    private void addDialogRow(JPanel panel, GridBagConstraints gbc, int row, String labelText, JComponent field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.4;
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(UITheme.FONT_BOLD);
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.6;
        if (field instanceof JTextField) {
            ((JTextField) field).setBorder(new CompoundBorder(
                    new LineBorder(UITheme.BORDER_DARK, 1),
                    new EmptyBorder(4, 6, 4, 6)
            ));
        }
        panel.add(field, gbc);
    }

    public void refreshAllData() {
        loadCoursesData();
        loadStudentsData();
        loadInstructorsData();
        loadAnalyticsData();
    }
}

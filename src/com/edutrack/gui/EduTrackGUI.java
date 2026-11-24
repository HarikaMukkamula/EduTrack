package com.edutrack.gui;

import com.edutrack.data.DataStore;
import com.edutrack.data.DataStoreFactory;
import com.edutrack.data.InMemoryDataStore;
import com.edutrack.model.Admin;
import com.edutrack.model.Instructor;
import com.edutrack.model.Role;
import com.edutrack.model.Student;
import com.edutrack.model.User;
import com.edutrack.service.CourseService;
import com.edutrack.service.EnrollmentService;
import com.edutrack.service.ReportService;
import com.edutrack.service.UserService;

import javax.swing.*;
import java.awt.*;

/**
 * EduTrackGUI: Main Swing Application Window.
 * Coordinates views using a CardLayout container hosting the Login Panel,
 * Administrator Dashboard, Faculty Instructor Portal, and Student Self-Service Portal.
 */
public class EduTrackGUI extends JFrame {
    private static final String CARD_LOGIN = "CARD_LOGIN";
    private static final String CARD_ADMIN = "CARD_ADMIN";
    private static final String CARD_INSTRUCTOR = "CARD_INSTRUCTOR";
    private static final String CARD_STUDENT = "CARD_STUDENT";

    private final DataStore dataStore;
    private final UserService userService;
    private final CourseService courseService;
    private final EnrollmentService enrollmentService;
    private final ReportService reportService;

    private CardLayout cardLayout;
    private JPanel mainContainer;

    private LoginPanel loginPanel;
    private AdminDashboardPanel adminPanel;
    private InstructorDashboardPanel instructorPanel;
    private StudentDashboardPanel studentPanel;

    private User currentUser = null;

    public EduTrackGUI(String dataDir) {
        super("EduTrack: Course Management System | GCET");

        // Initialize Services & Persistence via DataStoreFactory
        this.dataStore = DataStoreFactory.createDataStore(dataDir);
        this.userService = new UserService(dataStore);
        this.courseService = new CourseService(dataStore);
        this.enrollmentService = new EnrollmentService(dataStore, courseService);
        this.reportService = new ReportService(dataStore, courseService, enrollmentService);

        initWindow();
        initPanels();
    }

    private void initWindow() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1140, 740);
        setMinimumSize(new Dimension(960, 640));
        setLocationRelativeTo(null); // Center on screen
    }

    private void initPanels() {
        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);

        // 1. Login Panel
        loginPanel = new LoginPanel(this, userService, this::handleLoginSuccess);

        // 2. Admin Dashboard Panel
        adminPanel = new AdminDashboardPanel(this, userService, courseService, enrollmentService, reportService, this::handleLogout);

        // 3. Instructor Dashboard Panel
        instructorPanel = new InstructorDashboardPanel(this, userService, courseService, enrollmentService, reportService, this::handleLogout);

        // 4. Student Dashboard Panel
        studentPanel = new StudentDashboardPanel(this, userService, courseService, enrollmentService, this::handleLogout);

        mainContainer.add(loginPanel, CARD_LOGIN);
        mainContainer.add(adminPanel, CARD_ADMIN);
        mainContainer.add(instructorPanel, CARD_INSTRUCTOR);
        mainContainer.add(studentPanel, CARD_STUDENT);

        setContentPane(mainContainer);
        showCard(CARD_LOGIN);
    }

    private void showCard(String cardName) {
        cardLayout.show(mainContainer, cardName);
    }

    private void handleLoginSuccess(User user) {
        this.currentUser = user;
        if (user.getRole() == Role.ADMIN && user instanceof Admin) {
            adminPanel.setCurrentAdmin((Admin) user);
            showCard(CARD_ADMIN);
        } else if (user.getRole() == Role.INSTRUCTOR && user instanceof Instructor) {
            instructorPanel.setCurrentInstructor((Instructor) user);
            showCard(CARD_INSTRUCTOR);
        } else if (user.getRole() == Role.STUDENT && user instanceof Student) {
            studentPanel.setCurrentStudent((Student) user);
            showCard(CARD_STUDENT);
        }
    }

    private void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to log out of EduTrack?",
                "Logout Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            this.currentUser = null;
            loginPanel.resetForm();
            showCard(CARD_LOGIN);
        }
    }

    public static void main(String[] args) {
        // Set System Native Look & Feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Fallback to default Swing Look & Feel
        }

        String dataDir = "data";
        if (args.length > 0) {
            dataDir = args[0];
        }

        final String finalDataDir = dataDir;
        SwingUtilities.invokeLater(() -> {
            EduTrackGUI gui = new EduTrackGUI(finalDataDir);
            gui.setVisible(true);
        });
    }
}

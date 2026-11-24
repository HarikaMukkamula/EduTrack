package com.edutrack.gui;

import com.edutrack.data.DataStoreFactory;
import com.edutrack.exception.AuthenticationException;
import com.edutrack.model.Role;
import com.edutrack.model.User;
import com.edutrack.service.UserService;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.function.Consumer;

/**
 * LoginPanel: Elegant sign-in screen featuring 1-click test credentials,
 * university branding, and student self-registration integration.
 */
public class LoginPanel extends JPanel {
    private final UserService userService;
    private final Consumer<User> onLoginSuccess;
    private final Frame parentFrame;

    private JTextField txtIdentifier;
    private JPasswordField txtPassword;
    private JLabel lblStatus;

    public LoginPanel(Frame parentFrame, UserService userService, Consumer<User> onLoginSuccess) {
        this.parentFrame = parentFrame;
        this.userService = userService;
        this.onLoginSuccess = onLoginSuccess;

        setLayout(new GridBagLayout());
        setBackground(UITheme.BG_LIGHT);

        initUI();
    }

    private void initUI() {
        // Center Card Panel
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
                new LineBorder(UITheme.BORDER, 1),
                new EmptyBorder(32, 40, 32, 40)
        ));
        card.setPreferredSize(new Dimension(480, 560));
        card.setMaximumSize(new Dimension(500, 580));

        // 1. Institution Header
        JLabel lblLogo = new JLabel("🎓", SwingConstants.CENTER);
        lblLogo.setFont(new Font("Segoe UI", Font.PLAIN, 40));
        lblLogo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblTitle = new JLabel("EduTrack: Course Management", SwingConstants.CENTER);
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.PRIMARY);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSubtitle = new JLabel("Geethanjali College of Engineering & Technology (GCET)", SwingConstants.CENTER);
        lblSubtitle.setFont(UITheme.FONT_SMALL);
        lblSubtitle.setForeground(UITheme.TEXT_MUTED);
        lblSubtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblDbStatus = new JLabel(DataStoreFactory.isUsingMySql() ? "🗄️ Database: MySQL edutrack_db (Live Connected 🟢)" : "📁 Database: In-Memory File Storage", SwingConstants.CENTER);
        lblDbStatus.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblDbStatus.setForeground(DataStoreFactory.isUsingMySql() ? new Color(22, 163, 74) : UITheme.TEXT_MUTED);
        lblDbStatus.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(lblLogo);
        card.add(Box.createVerticalStrut(4));
        card.add(lblTitle);
        card.add(Box.createVerticalStrut(2));
        card.add(lblSubtitle);
        card.add(Box.createVerticalStrut(4));
        card.add(lblDbStatus);
        card.add(Box.createVerticalStrut(20));

        // 2. Input Fields
        JLabel lblId = new JLabel("User ID or University Email:");
        lblId.setFont(UITheme.FONT_BOLD);
        lblId.setForeground(UITheme.TEXT_BODY);
        lblId.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtIdentifier = new JTextField();
        txtIdentifier.setFont(UITheme.FONT_BODY);
        txtIdentifier.setBorder(new CompoundBorder(
                new LineBorder(UITheme.BORDER_DARK, 1),
                new EmptyBorder(8, 10, 8, 10)
        ));
        txtIdentifier.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        txtIdentifier.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblPwd = new JLabel("Password:");
        lblPwd.setFont(UITheme.FONT_BOLD);
        lblPwd.setForeground(UITheme.TEXT_BODY);
        lblPwd.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtPassword = new JPasswordField();
        txtPassword.setFont(UITheme.FONT_BODY);
        txtPassword.setBorder(new CompoundBorder(
                new LineBorder(UITheme.BORDER_DARK, 1),
                new EmptyBorder(8, 10, 8, 10)
        ));
        txtPassword.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        txtPassword.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Enter key shortcut
        txtPassword.addActionListener(e -> performLogin());
        txtIdentifier.addActionListener(e -> performLogin());

        lblStatus = new JLabel(" ");
        lblStatus.setFont(UITheme.FONT_SMALL);
        lblStatus.setForeground(UITheme.DANGER);
        lblStatus.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton btnLogin = UITheme.createPrimaryButton("Sign In to Portal");
        btnLogin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btnLogin.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnLogin.addActionListener(e -> performLogin());

        JButton btnRegister = UITheme.createSecondaryButton("New Student? Self-Register Here");
        btnRegister.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        btnRegister.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnRegister.addActionListener(e -> openRegisterDialog());

        card.add(lblId);
        card.add(Box.createVerticalStrut(6));
        card.add(txtIdentifier);
        card.add(Box.createVerticalStrut(14));
        card.add(lblPwd);
        card.add(Box.createVerticalStrut(6));
        card.add(txtPassword);
        card.add(Box.createVerticalStrut(6));
        card.add(lblStatus);
        card.add(Box.createVerticalStrut(10));
        card.add(btnLogin);
        card.add(Box.createVerticalStrut(8));
        card.add(btnRegister);
        card.add(Box.createVerticalStrut(20));

        // 3. Quick 1-Click Demo Accounts Section
        JLabel lblQuick = new JLabel("── Quick 1-Click Demo Logins ──", SwingConstants.CENTER);
        lblQuick.setFont(UITheme.FONT_SMALL);
        lblQuick.setForeground(UITheme.TEXT_MUTED);
        lblQuick.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(lblQuick);
        card.add(Box.createVerticalStrut(10));

        JPanel demoGrid = new JPanel(new GridLayout(2, 2, 8, 8));
        demoGrid.setOpaque(false);
        demoGrid.setAlignmentX(Component.LEFT_ALIGNMENT);
        demoGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

        demoGrid.add(createDemoButton("Admin: Jenkins", "admin@gcet.edu.in", "admin123"));
        demoGrid.add(createDemoButton("Faculty: Turing", "turing@gcet.edu.in", "prof123"));
        demoGrid.add(createDemoButton("Student: Harika", "harika@gcet.edu.in", "student123"));
        demoGrid.add(createDemoButton("Student: Rahul", "rahul@gcet.edu.in", "student123"));

        card.add(demoGrid);

        add(card);
    }

    private JButton createDemoButton(String label, String email, String password) {
        JButton btn = new JButton(label);
        btn.setFont(UITheme.FONT_SMALL);
        btn.setForeground(UITheme.PRIMARY);
        btn.setBackground(UITheme.PRIMARY_LIGHT);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new CompoundBorder(
                new LineBorder(new Color(199, 210, 254), 1),
                new EmptyBorder(6, 6, 6, 6)
        ));
        btn.addActionListener(e -> {
            txtIdentifier.setText(email);
            txtPassword.setText(password);
            performLogin();
        });
        return btn;
    }

    private void performLogin() {
        String identifier = txtIdentifier.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        if (identifier.isEmpty() || password.isEmpty()) {
            lblStatus.setText("Please enter both User ID/Email and Password.");
            return;
        }

        try {
            User user = userService.login(identifier, password);
            lblStatus.setText(" ");
            txtPassword.setText("");
            if (onLoginSuccess != null) {
                onLoginSuccess.accept(user);
            }
        } catch (AuthenticationException ex) {
            lblStatus.setText(ex.getMessage());
        }
    }

    private void openRegisterDialog() {
        RegistrationDialog dlg = new RegistrationDialog(parentFrame, userService);
        dlg.setVisible(true);
        if (dlg.isRegistered() && dlg.getCreatedStudent() != null) {
            txtIdentifier.setText(dlg.getCreatedStudent().getEmail());
            txtPassword.setText(dlg.getCreatedStudent().getPassword());
            lblStatus.setText("Registered as " + dlg.getCreatedStudent().getId() + "! Click Sign In.");
            lblStatus.setForeground(UITheme.SUCCESS);
        }
    }

    public void resetForm() {
        txtIdentifier.setText("");
        txtPassword.setText("");
        lblStatus.setText(" ");
        lblStatus.setForeground(UITheme.DANGER);
    }
}

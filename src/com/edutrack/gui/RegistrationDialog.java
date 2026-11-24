package com.edutrack.gui;

import com.edutrack.model.Student;
import com.edutrack.service.UserService;
import com.edutrack.util.ValidationUtils;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * RegistrationDialog: Student self-registration dialog with validation.
 */
public class RegistrationDialog extends JDialog {
    private final UserService userService;
    private boolean registered = false;
    private Student createdStudent = null;

    private JTextField txtName;
    private JTextField txtEmail;
    private JPasswordField txtPassword;
    private JTextField txtMajor;
    private JComboBox<Integer> cmbYear;
    private JComboBox<Integer> cmbSemester;
    private JTextField txtPhone;

    public RegistrationDialog(Frame parent, UserService userService) {
        super(parent, "Student Self-Registration | EduTrack GCET", true);
        this.userService = userService;
        initComponents();
        setSize(480, 520);
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(UITheme.BG_LIGHT);

        // Header
        JPanel headerPanel = new JPanel(new GridLayout(2, 1, 0, 4));
        headerPanel.setBackground(UITheme.PRIMARY);
        headerPanel.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel title = new JLabel("📝 Student Registration Form");
        title.setFont(UITheme.FONT_HEADER);
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel("Join the EduTrack Course Management System at GCET");
        subtitle.setFont(UITheme.FONT_SMALL);
        subtitle.setForeground(new Color(203, 213, 225));

        headerPanel.add(title);
        headerPanel.add(subtitle);
        add(headerPanel, BorderLayout.NORTH);

        // Form Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(new EmptyBorder(20, 24, 20, 24));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 4, 6, 4);

        txtName = new JTextField();
        txtEmail = new JTextField("@gcet.edu.in");
        txtPassword = new JPasswordField();
        txtMajor = new JTextField("CSE");
        cmbYear = new JComboBox<>(new Integer[]{1, 2, 3, 4});
        cmbYear.setSelectedItem(2);
        cmbSemester = new JComboBox<>(new Integer[]{1, 2, 3, 4, 5, 6, 7, 8});
        cmbSemester.setSelectedItem(3);
        txtPhone = new JTextField();

        addFormField(formPanel, gbc, 0, "Full Legal Name *:", txtName);
        addFormField(formPanel, gbc, 1, "University Email *:", txtEmail);
        addFormField(formPanel, gbc, 2, "Password *:", txtPassword);
        addFormField(formPanel, gbc, 3, "Academic Major *:", txtMajor);
        addFormField(formPanel, gbc, 4, "Year of Study *:", cmbYear);
        addFormField(formPanel, gbc, 5, "Current Semester *:", cmbSemester);
        addFormField(formPanel, gbc, 6, "Contact Phone Number:", txtPhone);

        add(formPanel, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        buttonPanel.setBackground(UITheme.BG_LIGHT);
        buttonPanel.setBorder(new LineBorder(UITheme.BORDER, 1));

        JButton btnCancel = UITheme.createSecondaryButton("Cancel");
        btnCancel.addActionListener(e -> dispose());

        JButton btnRegister = UITheme.createPrimaryButton("Create Account");
        btnRegister.addActionListener(e -> handleRegister());

        buttonPanel.add(btnCancel);
        buttonPanel.add(btnRegister);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void addFormField(JPanel panel, GridBagConstraints gbc, int row, String labelText, JComponent field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.35;
        JLabel label = new JLabel(labelText);
        label.setFont(UITheme.FONT_BOLD);
        label.setForeground(UITheme.TEXT_BODY);
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.65;
        if (field instanceof JTextField) {
            field.setFont(UITheme.FONT_BODY);
            ((JTextField) field).setBorder(new CompoundBorder(
                    new LineBorder(UITheme.BORDER_DARK, 1),
                    new EmptyBorder(6, 8, 6, 8)
            ));
        }
        panel.add(field, gbc);
    }

    private void handleRegister() {
        String name = txtName.getText().trim();
        String email = txtEmail.getText().trim().toLowerCase();
        String password = new String(txtPassword.getPassword()).trim();
        String major = txtMajor.getText().trim();
        int year = (Integer) cmbYear.getSelectedItem();
        int semester = (Integer) cmbSemester.getSelectedItem();
        String phone = txtPhone.getText().trim();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || major.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please fill in all required fields (Name, Email, Password, Major).",
                    "Missing Information", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!ValidationUtils.isValidEmail(email)) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a valid email format (e.g., student@gcet.edu.in).",
                    "Invalid Email", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            createdStudent = userService.registerStudent(name, email, password, major, year, semester, phone);
            registered = true;
            JOptionPane.showMessageDialog(this,
                    "Registration Successful!\n\nYour assigned Student ID is: " + createdStudent.getId() +
                            "\nYou can now log in with your ID or Email.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Registration failed: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isRegistered() {
        return registered;
    }

    public Student getCreatedStudent() {
        return createdStudent;
    }
}

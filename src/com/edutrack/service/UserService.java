package com.edutrack.service;

import com.edutrack.data.DataStore;
import com.edutrack.exception.AuthenticationException;
import com.edutrack.exception.RecordNotFoundException;
import com.edutrack.model.*;

import java.util.List;
import java.util.Optional;

/**
 * Service managing user operations: Authentication, Registration, and Profile Management.
 * Demonstrates:
 * - Single Responsibility Principle (SRP).
 * - Polymorphic User management.
 */
public class UserService {
    private final DataStore dataStore;

    public UserService(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    /**
     * Authenticates a user with either their User ID or registered Email address.
     */
    public User login(String idOrEmail, String password) throws AuthenticationException {
        if (idOrEmail == null || idOrEmail.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            throw new AuthenticationException("Credentials cannot be blank.");
        }

        String search = idOrEmail.trim();
        Optional<User> userOpt = dataStore.findUserById(search);

        if (userOpt.isEmpty()) {
            userOpt = dataStore.findUserByEmail(search);
        }

        if (userOpt.isEmpty()) {
            throw new AuthenticationException("No account found matching '" + search + "'.");
        }

        User user = userOpt.get();
        if (!user.authenticate(password.trim())) {
            throw new AuthenticationException("Incorrect password for account: " + user.getId());
        }

        return user;
    }

    public Student registerStudent(String name, String email, String password,
                                   String major, int year, int semester, String phone) throws Exception {
        if (dataStore.findUserByEmail(email).isPresent()) {
            throw new IllegalArgumentException("A user with email '" + email + "' already exists.");
        }
        String studentId = generateNextId(Role.STUDENT);
        Student student = new Student(studentId, name, email, password, major, year, semester, phone);
        dataStore.saveUser(student);
        return student;
    }

    public Instructor registerInstructor(String name, String email, String password,
                                         String department, String specialization) throws Exception {
        if (dataStore.findUserByEmail(email).isPresent()) {
            throw new IllegalArgumentException("A user with email '" + email + "' already exists.");
        }
        String instructorId = generateNextId(Role.INSTRUCTOR);
        Instructor instructor = new Instructor(instructorId, name, email, password, department, specialization);
        dataStore.saveUser(instructor);
        return instructor;
    }

    public Admin registerAdmin(String name, String email, String password,
                               String department, String adminLevel) throws Exception {
        if (dataStore.findUserByEmail(email).isPresent()) {
            throw new IllegalArgumentException("A user with email '" + email + "' already exists.");
        }
        String adminId = generateNextId(Role.ADMIN);
        Admin admin = new Admin(adminId, name, email, password, department, adminLevel);
        dataStore.saveUser(admin);
        return admin;
    }

    public User getUserById(String id) throws RecordNotFoundException {
        return dataStore.findUserById(id)
                .orElseThrow(() -> new RecordNotFoundException("User not found with ID: " + id));
    }

    public List<User> getUsersByRole(Role role) {
        return dataStore.getUsersByRole(role);
    }

    public List<User> getAllUsers() {
        return dataStore.getAllUsers();
    }

    public boolean removeUser(String id) throws RecordNotFoundException {
        if (!dataStore.deleteUser(id)) {
            throw new RecordNotFoundException("Cannot delete: user ID '" + id + "' not found.");
        }
        return true;
    }

    public void updateUserPassword(String userId, String newPassword) throws RecordNotFoundException {
        User user = getUserById(userId);
        user.setPassword(newPassword);
        dataStore.saveUser(user);
    }

    private synchronized String generateNextId(Role role) {
        String prefix = role == Role.ADMIN ? "ADM" : (role == Role.INSTRUCTOR ? "INS" : "STU");
        int max = 100;
        for (User u : dataStore.getUsersByRole(role)) {
            if (u.getId().startsWith(prefix)) {
                try {
                    int num = Integer.parseInt(u.getId().substring(prefix.length()));
                    if (num > max) max = num;
                } catch (NumberFormatException ignored) {}
            }
        }
        return prefix + (max + 1);
    }
}

package com.edutrack.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Abstract base class representing a Person/User in the EduTrack system.
 * Demonstrates:
 * - Abstraction: Cannot be directly instantiated; provides template methods for subclasses.
 * - Encapsulation: Private members with validated public accessors.
 */
public abstract class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String id;
    private String name;
    private String email;
    private String password;
    private final Role role;

    public User(String id, String name, String email, String password, Role role) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be empty.");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("User name cannot be empty.");
        }
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("A valid email address is required.");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty.");
        }
        if (role == null) {
            throw new IllegalArgumentException("User role must be specified.");
        }

        this.id = id.trim();
        this.name = name.trim();
        this.email = email.trim().toLowerCase();
        this.password = password.trim();
        this.role = role;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty.");
        }
        this.name = name.trim();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Valid email required.");
        }
        this.email = email.trim().toLowerCase();
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty.");
        }
        this.password = password.trim();
    }

    public Role getRole() {
        return role;
    }

    public boolean authenticate(String inputPassword) {
        return this.password.equals(inputPassword);
    }

    /**
     * Polymorphic method to display role-specific details.
     * Each subclass provides its own formatted summary.
     */
    public abstract String getRoleSpecificDetails();

    /**
     * Abstract method returning department or academic affiliation.
     */
    public abstract String getAffiliation();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("[%s] ID: %s | Name: %s | Email: %s", role.getDisplayName(), id, name, email);
    }
}

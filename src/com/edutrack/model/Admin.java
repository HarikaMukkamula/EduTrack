package com.edutrack.model;

/**
 * Represents a University Administrator.
 * Demonstrates:
 * - Inheritance: Extends User.
 * - Polymorphism: Overrides abstract methods to provide Admin-specific behavior.
 */
public class Admin extends User {
    private static final long serialVersionUID = 1L;

    private String department;
    private String adminLevel; // Super Admin, Academic Registrar, Department Head

    public Admin(String id, String name, String email, String password, String department, String adminLevel) {
        super(id, name, email, password, Role.ADMIN);
        this.department = (department == null || department.trim().isEmpty()) ? "Academic Administration" : department.trim();
        this.adminLevel = (adminLevel == null || adminLevel.trim().isEmpty()) ? "System Registrar" : adminLevel.trim();
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getAdminLevel() {
        return adminLevel;
    }

    public void setAdminLevel(String adminLevel) {
        this.adminLevel = adminLevel;
    }

    @Override
    public String getAffiliation() {
        return department;
    }

    @Override
    public String getRoleSpecificDetails() {
        return String.format("Admin Level: %s | Office Dept: %s", adminLevel, department);
    }

    @Override
    public String toString() {
        return super.toString() + String.format(" | Level: %s | Dept: %s", adminLevel, department);
    }
}

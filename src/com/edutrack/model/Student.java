package com.edutrack.model;

/**
 * Represents an enrolled Student at the University.
 * Demonstrates:
 * - Inheritance: Extends User base class.
 * - Encapsulation: Validates year of study, major, and provides clean state representation.
 * - Polymorphism: Customizes student-specific profile views.
 */
public class Student extends User {
    private static final long serialVersionUID = 1L;

    private String major;
    private int yearOfStudy; // 1 to 4
    private int currentSemester; // 1 to 8
    private String contactNumber;

    public Student(String id, String name, String email, String password,
                   String major, int yearOfStudy, int currentSemester, String contactNumber) {
        super(id, name, email, password, Role.STUDENT);
        this.major = (major == null || major.trim().isEmpty()) ? "Computer Science" : major.trim();
        this.yearOfStudy = (yearOfStudy >= 1 && yearOfStudy <= 6) ? yearOfStudy : 1;
        this.currentSemester = (currentSemester >= 1 && currentSemester <= 12) ? currentSemester : 1;
        this.contactNumber = (contactNumber == null || contactNumber.trim().isEmpty()) ? "N/A" : contactNumber.trim();
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        if (major != null && !major.trim().isEmpty()) {
            this.major = major.trim();
        }
    }

    public int getYearOfStudy() {
        return yearOfStudy;
    }

    public void setYearOfStudy(int yearOfStudy) {
        if (yearOfStudy >= 1 && yearOfStudy <= 6) {
            this.yearOfStudy = yearOfStudy;
        }
    }

    public int getCurrentSemester() {
        return currentSemester;
    }

    public void setCurrentSemester(int currentSemester) {
        if (currentSemester >= 1 && currentSemester <= 12) {
            this.currentSemester = currentSemester;
        }
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    @Override
    public String getAffiliation() {
        return major;
    }

    @Override
    public String getRoleSpecificDetails() {
        return String.format("Major: %s | Year: %d | Semester: %d | Phone: %s",
                major, yearOfStudy, currentSemester, contactNumber);
    }

    @Override
    public String toString() {
        return super.toString() + String.format(" | Major: %s | Year: %d (Sem %d)", major, yearOfStudy, currentSemester);
    }
}

package com.edutrack.model;

public class User {
    private int userId;
    private String name;
    private String email;
    private String role;
    private String course; // NEW: Added to hold the student's course

    // Existing constructor (so we don't break the Admin panel)
    public User(int userId, String name, String email, String role) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.role = role;
        this.course = "Unassigned";
    }

    // NEW constructor specifically for the Teacher Dashboard
    public User(int userId, String name, String email, String role, String course) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.role = role;
        // If they don't have a course yet, show "Unassigned" instead of blank
        this.course = (course != null) ? course : "Unassigned"; 
    }

    public int getUserId() { return userId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    
    // NEW: Getter so the table can read the course
    public String getCourse() { return course; }
}
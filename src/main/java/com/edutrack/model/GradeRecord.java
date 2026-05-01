package com.edutrack.model;

public class GradeRecord {
    private String subject;
    private String grade;

    public GradeRecord(String subject, String grade) {
        this.subject = subject;
        this.grade = grade;
    }

    public String getSubject() { 
        return subject; 
    }
    
    public String getGrade() { 
        return grade; 
    }
}
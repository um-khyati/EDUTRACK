package com.edutrack.model;

public class FeedbackRecord {
    private String fromUser;
    private String message;
    private String date;

    public FeedbackRecord(String fromUser, String message, String date) {
        this.fromUser = fromUser;
        this.message = message;
        this.date = date;
    }

    public String getFromUser() { return fromUser; }
    public String getMessage() { return message; }
    public String getDate() { return date; }
}
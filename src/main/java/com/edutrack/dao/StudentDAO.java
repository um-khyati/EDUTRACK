package com.edutrack.dao;

import com.edutrack.model.AttendanceRecord;
import com.edutrack.model.GradeRecord;
import com.edutrack.model.FeedbackRecord;
import com.edutrack.model.User;
import com.edutrack.util.DBConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StudentDAO {

    public ObservableList<AttendanceRecord> getMyAttendance(int studentId) {
        ObservableList<AttendanceRecord> attendanceList = FXCollections.observableArrayList();
        String query = "SELECT date, status FROM attendance WHERE student_id = ?";
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, studentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    attendanceList.add(new AttendanceRecord(
                            rs.getDate("date").toString(),
                            rs.getString("status")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return attendanceList;
    }

    public ObservableList<GradeRecord> getMyGrades(int studentId) {
        ObservableList<GradeRecord> gradeList = FXCollections.observableArrayList();
        String query = "SELECT subject, grade FROM grades WHERE student_id = ?";
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, studentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    gradeList.add(new GradeRecord(
                            rs.getString("subject"),
                            rs.getString("grade")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return gradeList;
    }

    public String getStudentCourse(int studentId) {
        String query = "SELECT course FROM students WHERE user_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, studentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("course");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Unassigned";
    }

    public boolean updateStudentCourse(int studentId, String newCourse) {
        try (Connection conn = DBConnection.getInstance().getConnection()) {
            
            boolean exists = false;
            String checkQuery = "SELECT * FROM students WHERE user_id = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
                checkStmt.setInt(1, studentId);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    exists = rs.next();
                }
            }
            
            if (exists) {
                String updateQuery = "UPDATE students SET course = ? WHERE user_id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(updateQuery)) {
                    pstmt.setString(1, newCourse);
                    pstmt.setInt(2, studentId);
                    return pstmt.executeUpdate() > 0;
                }
            } else {
                // THE FIX: Use a unique roll number based on their ID to avoid duplicates!
                String uniqueRollNo = "TBD-" + studentId;
                
                String insertQuery = "INSERT INTO students (user_id, roll_no, course, year) VALUES (?, ?, ?, 1)";
                try (PreparedStatement pstmt = conn.prepareStatement(insertQuery)) {
                    pstmt.setInt(1, studentId);
                    pstmt.setString(2, uniqueRollNo);
                    pstmt.setString(3, newCourse);
                    return pstmt.executeUpdate() > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error while updating course!");
            e.printStackTrace();
            return false;
        }
    }

    public ObservableList<FeedbackRecord> getMyFeedback(int studentId) {
        ObservableList<FeedbackRecord> feedbackList = FXCollections.observableArrayList();
        String query = "SELECT " +
                       "  u_from.name AS display_name, f.message, f.created_at " +
                       "FROM feedback f " +
                       "JOIN users u_from ON f.from_user_id = u_from.user_id " +
                       "WHERE f.to_user_id = ? " +
                       "ORDER BY f.created_at DESC";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            if (conn == null) {
                System.err.println("ERROR: Database connection is null!");
                return feedbackList;
            }
            pstmt.setInt(1, studentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    feedbackList.add(new FeedbackRecord(
                            rs.getString("display_name"),
                            rs.getString("message"),
                            rs.getTimestamp("created_at").toString()
                    ));
                }
                System.out.println("Retrieved " + feedbackList.size() + " feedback records for student " + studentId);
            }
        } catch (SQLException e) {
            System.err.println("ERROR retrieving feedback: " + e.getMessage());
            e.printStackTrace();
        }
        return feedbackList;
    }

    public boolean submitFeedbackToTeacher(int studentId, int teacherId, String feedback) {
        try (Connection conn = DBConnection.getInstance().getConnection()) {
            if (conn == null) {
                System.err.println("ERROR: Database connection is null!");
                return false;
            }

            try (java.sql.Statement stmt = conn.createStatement()) {
                stmt.execute("CREATE TABLE IF NOT EXISTS feedback (" +
                        "feedback_id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "from_user_id INT NOT NULL, " +
                        "to_user_id INT NOT NULL, " +
                        "message TEXT NOT NULL, " +
                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
                System.out.println("Feedback table verified/created successfully.");
            } catch (Exception e) {
                System.err.println("Error creating feedback table: " + e.getMessage());
                e.printStackTrace();
            }

            String query = "INSERT INTO feedback (from_user_id, to_user_id, message) VALUES (?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setInt(1, studentId);
                pstmt.setInt(2, teacherId);
                pstmt.setString(3, feedback);
                int result = pstmt.executeUpdate();
                System.out.println("Feedback submitted successfully. Student: " + studentId + ", Teacher: " + teacherId);
                return result > 0;
            }
        } catch (SQLException e) {
            System.err.println("ERROR submitting feedback: " + e.getMessage());
            System.err.println("SQL State: " + e.getSQLState());
            e.printStackTrace();
            return false;
        }
    }

    public ObservableList<User> getAllTeachers() {
        ObservableList<User> teacherList = FXCollections.observableArrayList();
        String query = "SELECT user_id, name, email, role FROM users WHERE role = 'TEACHER'";
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                teacherList.add(new User(
                        rs.getInt("user_id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("role"),
                        null
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return teacherList;
    }
}
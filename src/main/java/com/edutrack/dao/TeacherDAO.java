package com.edutrack.dao;

import com.edutrack.model.FeedbackRecord;
import com.edutrack.model.User;
import com.edutrack.util.DBConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TeacherDAO {

    public ObservableList<User> getAllStudents() {
        ObservableList<User> studentList = FXCollections.observableArrayList();
        
        // NEW: We JOIN the users table and students table together!
        String query = "SELECT u.user_id, u.name, u.email, u.role, s.course " +
                       "FROM users u " +
                       "LEFT JOIN students s ON u.user_id = s.user_id " +
                       "WHERE u.role = 'STUDENT'";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                studentList.add(new User(
                        rs.getInt("user_id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("role"),
                        rs.getString("course") // We grab the new course data!
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching students!");
            e.printStackTrace();
        }
        return studentList;
    }

    public boolean markAttendance(int studentId, int teacherId, String status) {
        java.sql.Date today = new java.sql.Date(System.currentTimeMillis());

        String disableFK = "SET FOREIGN_KEY_CHECKS=0";
        String enableFK = "SET FOREIGN_KEY_CHECKS=1";
        String query = "INSERT INTO attendance (student_id, teacher_id, date, status) VALUES (?, ?, ?, ?) " +
                       "ON DUPLICATE KEY UPDATE status = ?, teacher_id = ?";

        try (Connection conn = DBConnection.getInstance().getConnection()) {
            
            try (PreparedStatement stmtDisable = conn.prepareStatement(disableFK)) {
                stmtDisable.execute();
            }

            boolean success = false;
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setInt(1, studentId);
                pstmt.setInt(2, teacherId);
                pstmt.setDate(3, today);
                pstmt.setString(4, status);
                pstmt.setString(5, status);
                pstmt.setInt(6, teacherId);
                success = pstmt.executeUpdate() > 0;
            }

            try (PreparedStatement stmtEnable = conn.prepareStatement(enableFK)) {
                stmtEnable.execute();
            }

            return success;

        } catch (SQLException e) {
            System.err.println("Database error while marking attendance!");
            e.printStackTrace();
            return false;
        }
    }

    public boolean assignGrade(int studentId, int teacherId, String subject, String grade) {
        try (Connection conn = DBConnection.getInstance().getConnection()) {
            
            // --- THE AUTO-HEALER ---
            try (java.sql.Statement stmt = conn.createStatement()) {
                stmt.execute("INSERT INTO grades (student_id, teacher_id, subject, grade) VALUES (-1, -1, 'test', 'test')");
                stmt.execute("DELETE FROM grades WHERE student_id = -1");
            } catch (Exception e) {
                try (java.sql.Statement rebuild = conn.createStatement()) {
                    rebuild.execute("DROP TABLE IF EXISTS grades");
                    rebuild.execute("CREATE TABLE grades (" +
                            "grade_id INT AUTO_INCREMENT PRIMARY KEY, " +
                            "student_id INT, " +
                            "teacher_id INT, " +
                            "subject VARCHAR(100), " +
                            "grade VARCHAR(10))");
                }
            }
            // -----------------------

            String query = "INSERT INTO grades (student_id, teacher_id, subject, grade) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setInt(1, studentId);
                pstmt.setInt(2, teacherId);
                pstmt.setString(3, subject);
                pstmt.setString(4, grade);
                return pstmt.executeUpdate() > 0;
            }
            
        } catch (SQLException e) {
            System.err.println("Error assigning grade!");
            e.printStackTrace();
            return false;
        }
    }

    public boolean submitFeedback(int teacherId, int studentId, String feedback) {
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
                pstmt.setInt(1, teacherId);
                pstmt.setInt(2, studentId);
                pstmt.setString(3, feedback);
                int result = pstmt.executeUpdate();
                System.out.println("Feedback submitted successfully. Teacher: " + teacherId + ", Student: " + studentId);
                return result > 0;
            }
        } catch (SQLException e) {
            System.err.println("ERROR submitting feedback: " + e.getMessage());
            System.err.println("SQL State: " + e.getSQLState());
            e.printStackTrace();
            return false;
        }
    }

    public ObservableList<FeedbackRecord> getMyFeedback(int teacherId) {
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
            pstmt.setInt(1, teacherId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    feedbackList.add(new FeedbackRecord(rs.getString("display_name"), rs.getString("message"), rs.getTimestamp("created_at").toString()));
                }
                System.out.println("Retrieved " + feedbackList.size() + " feedback records for teacher " + teacherId);
            }
        } catch (SQLException e) {
            System.err.println("ERROR retrieving feedback: " + e.getMessage());
            e.printStackTrace();
        }
        return feedbackList;
    }
}
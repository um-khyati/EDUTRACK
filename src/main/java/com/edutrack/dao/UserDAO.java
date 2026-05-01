package com.edutrack.dao;

import com.edutrack.model.User;
import com.edutrack.util.DBConnection;
import com.edutrack.util.SecurityUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class UserDAO {

    public ObservableList<User> getAllUsers() {
        ObservableList<User> userList = FXCollections.observableArrayList();
        String query = "SELECT user_id, name, email, role FROM users";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                userList.add(new User(
                        rs.getInt("user_id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("role")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all users!");
            e.printStackTrace();
        }
        return userList;
    }

    public boolean addUser(String name, String email, String password, String role) {
        String insertUserQuery = "INSERT INTO users (name, email, password, role) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnection.getInstance().getConnection()) {

            try (PreparedStatement pstmt = conn.prepareStatement(insertUserQuery, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, name);
                pstmt.setString(2, email);
                
                // THE FIX: Hash the password before saving it so Login works!
                pstmt.setString(3, SecurityUtil.hashPassword(password)); 
                
                pstmt.setString(4, role);

                int affectedRows = pstmt.executeUpdate();

                if (affectedRows > 0) {
                    try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            int newUserId = generatedKeys.getInt(1);

                            if (role.equals("STUDENT")) {
                                String insertStudent = "INSERT INTO students (user_id, roll_no, course, year) VALUES (?, 'TBD', 'Unassigned', 1)";
                                try (PreparedStatement stmtStudent = conn.prepareStatement(insertStudent)) {
                                    stmtStudent.setInt(1, newUserId);
                                    stmtStudent.executeUpdate();
                                }
                            } else if (role.equals("TEACHER")) {
                                String insertTeacher = "INSERT INTO teachers (user_id, subject) VALUES (?, 'Unassigned')";
                                try (PreparedStatement stmtTeacher = conn.prepareStatement(insertTeacher)) {
                                    stmtTeacher.setInt(1, newUserId);
                                    stmtTeacher.executeUpdate();
                                }
                            }
                        }
                    }
                    return true; 
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error adding user!");
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteUser(int userId) {
        String query = "DELETE FROM users WHERE user_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
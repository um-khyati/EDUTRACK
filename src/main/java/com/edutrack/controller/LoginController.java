package com.edutrack.controller;

import com.edutrack.model.User;
import com.edutrack.util.DBConnection;
import com.edutrack.util.SecurityUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    public static User loggedInUser; // Basic session management

    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please enter both email and password.");
            return;
        }

        String hashedPassword = SecurityUtil.hashPassword(password);
        authenticateUser(email, hashedPassword);
    }

    private void authenticateUser(String email, String hashedPassword) {
        String query = "SELECT * FROM users WHERE email = ? AND password = ?";
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
             
            pstmt.setString(1, email);
            pstmt.setString(2, hashedPassword);
            
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                loggedInUser = new User(
                    rs.getInt("user_id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("role")
                );
                redirectBasedOnRole(loggedInUser.getRole());
            } else {
                showAlert(Alert.AlertType.ERROR, "Login Failed", "Invalid email or password.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Could not connect to the database.");
        }
    }

    private void redirectBasedOnRole(String role) {
        String viewFile = "";
        switch (role) {
            case "ADMIN": viewFile = "/views/AdminDashboard.fxml"; break;
            case "TEACHER": viewFile = "/views/TeacherDashboard.fxml"; break;
            case "STUDENT": viewFile = "/views/StudentDashboard.fxml"; break;
        }

        try {
            Parent root = FXMLLoader.load(getClass().getResource(viewFile));
            Stage stage = (Stage) emailField.getScene().getWindow();
            
            stage.setScene(new Scene(root, 800, 600));
            stage.centerOnScreen();
            
            // THE FIX: This single line forces the window to open in Full Screen!
            stage.setMaximized(true); 
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not load the dashboard: " + viewFile);
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
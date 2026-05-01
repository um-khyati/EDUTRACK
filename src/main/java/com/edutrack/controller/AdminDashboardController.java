package com.edutrack.controller;

import com.edutrack.dao.UserDAO;
import com.edutrack.model.User;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;

public class AdminDashboardController {

    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, Integer> colId;
    @FXML private TableColumn<User, String> colName;
    @FXML private TableColumn<User, String> colEmail;
    @FXML private TableColumn<User, String> colRole;

    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> roleComboBox;

    private UserDAO userDAO = new UserDAO();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("userId"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));

        roleComboBox.getItems().addAll("ADMIN", "TEACHER", "STUDENT");
        loadUsers();
    }

    private void loadUsers() {
        ObservableList<User> users = userDAO.getAllUsers();
        userTable.setItems(users);
    }

    @FXML
    private void handleAddUser() {
        String name = nameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        String role = roleComboBox.getValue();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || role == null) {
            showAlert(Alert.AlertType.WARNING, "Form Error", "Please fill in all fields.");
            return;
        }

        if (userDAO.addUser(name, email, password, role)) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "User added successfully!");
            clearFields();
            loadUsers();
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to add user.");
        }
    }

    @FXML
    private void handleDeleteUser() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Selection Error", "Please select a user to delete.");
            return;
        }

        if (userDAO.deleteUser(selected.getUserId())) {
            loadUsers();
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete user.");
        }
    }

    @FXML
    private void handleLogout() {
        try {
            LoginController.loggedInUser = null;
            Parent root = FXMLLoader.load(getClass().getResource("/views/Login.fxml"));
            Stage stage = (Stage) userTable.getScene().getWindow();
            
            // Go back to the larger login window size
            stage.setScene(new Scene(root, 600, 550));
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void clearFields() {
        nameField.clear();
        emailField.clear();
        passwordField.clear();
        roleComboBox.getSelectionModel().clearSelection();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
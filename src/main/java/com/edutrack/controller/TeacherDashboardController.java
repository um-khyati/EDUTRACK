package com.edutrack.controller;

import com.edutrack.dao.TeacherDAO;
import com.edutrack.model.FeedbackRecord;
import com.edutrack.model.User;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;

public class TeacherDashboardController {

    @FXML private TableView<User> studentTable;
    @FXML private TableColumn<User, Integer> colId;
    @FXML private TableColumn<User, String> colName;
    @FXML private TableColumn<User, String> colEmail;
    @FXML private TableColumn<User, String> colCourse; 

    @FXML private TextField subjectField;
    @FXML private TextField gradeField;

    @FXML private Label selectedStudentLabel;
    @FXML private TextField feedbackField;
    @FXML private TableView<FeedbackRecord> feedbackTable;
    @FXML private TableColumn<FeedbackRecord, String> colFeedbackFrom;
    @FXML private TableColumn<FeedbackRecord, String> colFeedbackMessage;
    @FXML private TableColumn<FeedbackRecord, String> colFeedbackDate;

    private TeacherDAO teacherDAO = new TeacherDAO();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("userId"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colCourse.setCellValueFactory(new PropertyValueFactory<>("course")); 

        if (colFeedbackFrom != null) {
            colFeedbackFrom.setCellValueFactory(new PropertyValueFactory<>("fromUser"));
            colFeedbackMessage.setCellValueFactory(new PropertyValueFactory<>("message"));
            colFeedbackDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        }

        loadStudents();
    }

    private void loadStudents() {
        ObservableList<User> students = teacherDAO.getAllStudents();
        studentTable.setItems(students);
        
        // Add listener to update selected student label
        studentTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedStudentLabel.setText(newVal.getName());
            } else {
                selectedStudentLabel.setText("None");
            }
        });
        
        if (feedbackTable != null && LoginController.loggedInUser != null) {
            feedbackTable.setItems(teacherDAO.getMyFeedback(LoginController.loggedInUser.getUserId()));
        }
    }

    @FXML
    private void handleMarkPresent() {
        processAttendance("PRESENT");
    }

    @FXML
    private void handleMarkAbsent() {
        processAttendance("ABSENT");
    }

    private void processAttendance(String status) {
        User selectedStudent = studentTable.getSelectionModel().getSelectedItem();
        if (selectedStudent == null) {
            showAlert(Alert.AlertType.WARNING, "Selection Error", "Please select a student first.");
            return;
        }

        int teacherId = LoginController.loggedInUser.getUserId();
        boolean success = teacherDAO.markAttendance(selectedStudent.getUserId(), teacherId, status);

        if (success) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Attendance marked as " + status + " for " + selectedStudent.getName());
        } else {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to mark attendance.");
        }
    }

    @FXML
    private void handleAssignGrade() {
        User selectedStudent = studentTable.getSelectionModel().getSelectedItem();
        
        if (selectedStudent == null) {
            showAlert(Alert.AlertType.WARNING, "Selection Error", "Please select a student first.");
            return;
        }

        String subject = subjectField.getText();
        String grade = gradeField.getText();

        if (subject == null || subject.trim().isEmpty() || grade == null || grade.trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Input Error", "Please enter both a Subject and a Grade.");
            return;
        }

        int teacherId = LoginController.loggedInUser.getUserId();
        boolean success = teacherDAO.assignGrade(selectedStudent.getUserId(), teacherId, subject, grade);

        if (success) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Grade '" + grade + "' assigned for " + subject + " to " + selectedStudent.getName());
            subjectField.clear(); 
            gradeField.clear();
        } else {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to assign grade.");
        }
    }

    @FXML
    private void handleSubmitFeedback() {
        User selectedStudent = studentTable.getSelectionModel().getSelectedItem();
        if (selectedStudent == null) {
            showAlert(Alert.AlertType.WARNING, "Selection Error", "Please select a student first.");
            return;
        }

        String feedback = feedbackField.getText();
        if (feedback == null || feedback.trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Input Error", "Please enter some feedback.");
            return;
        }

        int teacherId = LoginController.loggedInUser.getUserId();
        System.out.println("Attempting to submit feedback from teacher " + teacherId + " to student " + selectedStudent.getUserId());
        
        if (teacherDAO.submitFeedback(teacherId, selectedStudent.getUserId(), feedback)) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Feedback sent to " + selectedStudent.getName());
            feedbackField.clear();
            // Refresh the feedback table after submission
            refreshFeedbackTable();
        } else {
            System.err.println("ERROR: submitFeedback returned false");
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to send feedback. Check console for details.");
        }
    }

    private void refreshFeedbackTable() {
        if (feedbackTable != null && LoginController.loggedInUser != null) {
            int teacherId = LoginController.loggedInUser.getUserId();
            feedbackTable.setItems(teacherDAO.getMyFeedback(teacherId));
        }
    }

    @FXML
    private void handleLogout() {
        try {
            LoginController.loggedInUser = null;
            Parent root = FXMLLoader.load(getClass().getResource("/views/Login.fxml"));
            Stage stage = (Stage) studentTable.getScene().getWindow();
            
            // THE FIX: Logs out to the new, larger 600x550 window
            stage.setScene(new Scene(root, 600, 550)); 
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
package com.edutrack.controller;

import com.edutrack.dao.StudentDAO;
import com.edutrack.model.AttendanceRecord;
import com.edutrack.model.GradeRecord;
import com.edutrack.model.FeedbackRecord;
import com.edutrack.model.User;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class StudentDashboardController {

    @FXML private Label welcomeLabel;
    
    @FXML private Label courseLabel;
    @FXML private TextField courseInputField;
    
    @FXML private TableView<AttendanceRecord> attendanceTable;
    @FXML private TableColumn<AttendanceRecord, String> colDate;
    @FXML private TableColumn<AttendanceRecord, String> colStatus;

    @FXML private TableView<GradeRecord> gradeTable;
    @FXML private TableColumn<GradeRecord, String> colSubject;
    @FXML private TableColumn<GradeRecord, String> colGradeValue;

    @FXML private TableView<FeedbackRecord> feedbackTable;
    @FXML private TableColumn<FeedbackRecord, String> colFeedbackFrom;
    @FXML private TableColumn<FeedbackRecord, String> colFeedbackMessage;
    @FXML private TableColumn<FeedbackRecord, String> colFeedbackDate;

    @FXML private ComboBox<String> teacherComboBox;
    @FXML private TextField studentFeedbackField;

    private StudentDAO studentDAO = new StudentDAO();
    private Map<String, Integer> teacherNameToIdMap = new HashMap<>();

    @FXML
    public void initialize() {
        if (LoginController.loggedInUser != null) {
            welcomeLabel.setText(LoginController.loggedInUser.getName() + "'s Dashboard");

            colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
            colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
            colSubject.setCellValueFactory(new PropertyValueFactory<>("subject"));
            colGradeValue.setCellValueFactory(new PropertyValueFactory<>("grade"));

            if (colFeedbackFrom != null) {
                colFeedbackFrom.setCellValueFactory(new PropertyValueFactory<>("fromUser"));
                colFeedbackMessage.setCellValueFactory(new PropertyValueFactory<>("message"));
                colFeedbackDate.setCellValueFactory(new PropertyValueFactory<>("date"));
            }

            loadAllData();
        }
    }

    private void loadTeachers() {
        ObservableList<User> teachers = studentDAO.getAllTeachers();
        teacherNameToIdMap.clear();
        
        for (User teacher : teachers) {
            String teacherName = teacher.getName() + " (ID: " + teacher.getUserId() + ")";
            teacherNameToIdMap.put(teacherName, teacher.getUserId());
        }
        
        if (teacherComboBox != null) {
            teacherComboBox.getItems().clear();
            teacherComboBox.getItems().addAll(teacherNameToIdMap.keySet());
        }
    }

    private void loadAllData() {
        int currentUserId = LoginController.loggedInUser.getUserId();
        
        attendanceTable.setItems(studentDAO.getMyAttendance(currentUserId));
        gradeTable.setItems(studentDAO.getMyGrades(currentUserId));

        if (feedbackTable != null) {
            feedbackTable.setItems(studentDAO.getMyFeedback(currentUserId));
        }
        
        String currentCourse = studentDAO.getStudentCourse(currentUserId);
        courseLabel.setText(currentCourse);
        
        // Load teachers into ComboBox
        loadTeachers();
    }

    @FXML
    private void handleUpdateCourse() {
        String newCourse = courseInputField.getText();
        
        if (newCourse == null || newCourse.trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Input Error", "Please type a course name first.");
            return;
        }

        int currentUserId = LoginController.loggedInUser.getUserId();
        boolean success = studentDAO.updateStudentCourse(currentUserId, newCourse);

        if (success) {
            courseLabel.setText(newCourse);
            courseInputField.clear();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Your course has been updated!");
        } else {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to update course.");
        }
    }

    @FXML
    private void handleSubmitFeedback() {
        String selectedTeacher = teacherComboBox.getValue();
        String feedback = studentFeedbackField.getText();

        if (selectedTeacher == null || selectedTeacher.isEmpty() || feedback == null || feedback.trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Input Error", "Please select a Teacher and provide Feedback.");
            return;
        }

        Integer teacherId = teacherNameToIdMap.get(selectedTeacher);
        if (teacherId == null) {
            showAlert(Alert.AlertType.ERROR, "Selection Error", "Invalid teacher selection.");
            return;
        }

        int currentUserId = LoginController.loggedInUser.getUserId();

        System.out.println("Attempting to submit feedback from student " + currentUserId + " to teacher " + teacherId);
        
        if (studentDAO.submitFeedbackToTeacher(currentUserId, teacherId, feedback)) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Feedback submitted to " + selectedTeacher);
            teacherComboBox.setValue(null);
            studentFeedbackField.clear();
            // Refresh the feedback table after submission
            refreshFeedbackTable();
        } else {
            System.err.println("ERROR: submitFeedbackToTeacher returned false");
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to submit feedback. Check console for details.");
        }
    }

    private void refreshFeedbackTable() {
        if (feedbackTable != null && LoginController.loggedInUser != null) {
            int currentUserId = LoginController.loggedInUser.getUserId();
            feedbackTable.setItems(studentDAO.getMyFeedback(currentUserId));
        }
    }

    @FXML
    private void handleLogout() {
        try {
            LoginController.loggedInUser = null;
            Parent root = FXMLLoader.load(getClass().getResource("/views/Login.fxml"));
            Stage stage = (Stage) attendanceTable.getScene().getWindow();
            
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
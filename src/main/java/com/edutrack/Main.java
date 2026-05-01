package com.edutrack;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/views/Login.fxml"));
        primaryStage.setTitle("EduTrack - Student Management System");
        
        // Starts the app with a clean, spacious 600x550 window
        primaryStage.setScene(new Scene(root, 600, 550)); 
        
        // Unlocks the window so users can resize or maximize it
        primaryStage.setResizable(true); 
        
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
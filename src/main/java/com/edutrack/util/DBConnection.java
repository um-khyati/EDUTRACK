package com.edutrack.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static DBConnection instance;
    private Connection connection;

    // --- IMPORTANT: UPDATE THESE TO YOUR ACTUAL CREDENTIALS ---
    private final String URL = "jdbc:mysql://localhost:3306/edutrack";
    private final String USER = "root"; 
    private final String PASSWORD = "Nancynagin@7"; // <-- CHANGE THIS!

    private DBConnection() {
        // Left empty intentionally. We create the connection below now.
    }

    public static DBConnection getInstance() {
        if (instance == null) {
            instance = new DBConnection();
        }
        return instance;
    }

    // --- THE FIX: ALWAYS CHECK IF THE CONNECTION IS ALIVE ---
    public Connection getConnection() {
        try {
            // If the connection doesn't exist yet, OR if it died/closed, make a fresh one!
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
            }
        } catch (SQLException e) {
            System.out.println("Failed to revive database connection!");
            e.printStackTrace();
        }
        return connection;
    }
}
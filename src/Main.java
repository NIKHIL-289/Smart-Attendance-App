package com.attendance;

import com.attendance.dao.DatabaseConnection;
import com.attendance.ui.LoginFrame;

import javax.swing.*;

/**
 * Main entry point of the Smart Attendance Management Application.
 * Initializes the SQLite database and launches the Swing GUI.
 *
 * Advanced Java Concepts Used: SwingUtilities.invokeLater (EDT safety),
 * UIManager (Look and Feel), singleton initialization.
 */
public class Main {

    public static void main(String[] args) {
        // Initialize the database and create tables (Singleton pattern)
        DatabaseConnection.getInstance().initializeDatabase();

        // Launch GUI on the Event Dispatch Thread (Thread Safety in Swing)
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                System.err.println("Could not set system look and feel: " + e.getMessage());
            }
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}

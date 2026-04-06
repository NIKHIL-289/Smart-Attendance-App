package com.attendance.dao;

import java.sql.*;

/**
 * Singleton database connection manager for SQLite.
 *
 * Advanced Java Concepts Used:
 *  - Singleton Design Pattern (thread-safe with synchronized)
 *  - JDBC API (DriverManager, Connection, Statement)
 *  - try-with-resources for auto-closing resources
 */
public class DatabaseConnection {

    private static final String DB_URL = "jdbc:sqlite:attendance.db";
    private static DatabaseConnection instance;
    private Connection connection;

    // Private constructor – prevents external instantiation
    private DatabaseConnection() {}

    /**
     * Returns the single instance of this class (thread-safe Singleton).
     */
    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    /**
     * Opens (or re-opens) the SQLite connection.
     */
    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_URL);
            // Enable foreign key support in SQLite
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON");
            }
        }
        return connection;
    }

    /**
     * Creates all application tables if they don't already exist.
     * Also seeds a default admin account on first run.
     */
    public void initializeDatabase() {
        String createUsers = "CREATE TABLE IF NOT EXISTS users (" +
                "id       INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT    NOT NULL UNIQUE, " +
                "password TEXT    NOT NULL, " +
                "fullName TEXT    NOT NULL, " +
                "role     TEXT    NOT NULL DEFAULT 'TEACHER'" +
                ")";

        String createCourses = "CREATE TABLE IF NOT EXISTS courses (" +
                "id            INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "code          TEXT    NOT NULL UNIQUE, " +
                "name          TEXT    NOT NULL, " +
                "instructor    TEXT    NOT NULL, " +
                "totalClasses  INTEGER NOT NULL DEFAULT 0" +
                ")";

        String createStudents = "CREATE TABLE IF NOT EXISTS students (" +
                "id         INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "rollNumber TEXT    NOT NULL UNIQUE, " +
                "name       TEXT    NOT NULL, " +
                "email      TEXT, " +
                "phone      TEXT, " +
                "courseId   INTEGER NOT NULL, " +
                "FOREIGN KEY (courseId) REFERENCES courses(id) ON DELETE CASCADE" +
                ")";

        String createAttendance = "CREATE TABLE IF NOT EXISTS attendance (" +
                "id        INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "studentId INTEGER NOT NULL, " +
                "courseId  INTEGER NOT NULL, " +
                "date      TEXT    NOT NULL, " +
                "status    TEXT    NOT NULL DEFAULT 'PRESENT', " +
                "FOREIGN KEY (studentId) REFERENCES students(id) ON DELETE CASCADE, " +
                "FOREIGN KEY (courseId)  REFERENCES courses(id)  ON DELETE CASCADE, " +
                "UNIQUE (studentId, courseId, date)" +
                ")";

        String insertDefaultAdmin = "INSERT OR IGNORE INTO users (username, password, fullName, role) " +
                "VALUES ('admin', 'admin123', 'System Administrator', 'ADMIN')";

        try (Connection conn = getConnection();
             Statement  stmt = conn.createStatement()) {

            stmt.execute(createUsers);
            stmt.execute(createCourses);
            stmt.execute(createStudents);
            stmt.execute(createAttendance);
            stmt.execute(insertDefaultAdmin);

            System.out.println("Database initialized successfully.");
        } catch (SQLException e) {
            System.err.println("Database initialization error: " + e.getMessage());
        }
    }

    /** Closes the connection – call on application exit. */
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }
}

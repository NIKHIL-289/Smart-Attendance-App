package com.attendance.dao;

import com.attendance.exception.AttendanceException;
import com.attendance.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for User entities.
 * Implements GenericDAO with JDBC operations.
 *
 * Advanced Java Concepts: PreparedStatement (prevents SQL Injection),
 * Optional<T>, try-with-resources.
 */
public class UserDAO implements GenericDAO<User, Integer> {

    private final DatabaseConnection dbConn = DatabaseConnection.getInstance();

    @Override
    public User save(User user) throws AttendanceException {
        String sql = "INSERT INTO users (username, password, fullName, role) VALUES (?, ?, ?, ?)";
        try (Connection conn = dbConn.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getFullName());
            ps.setString(4, user.getRole().name());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) user.setId(keys.getInt(1));
            }
            return user;
        } catch (SQLException e) {
            if (e.getMessage().contains("UNIQUE")) {
                throw new AttendanceException("Username already exists.", AttendanceException.DUPLICATE_ENTRY);
            }
            throw new AttendanceException("Error saving user: " + e.getMessage(), AttendanceException.DB_ERROR);
        }
    }

    @Override
    public Optional<User> findById(Integer id) throws AttendanceException {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = dbConn.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new AttendanceException("Error finding user: " + e.getMessage(), AttendanceException.DB_ERROR);
        }
    }

    @Override
    public List<User> findAll() throws AttendanceException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY fullName";
        try (Connection conn = dbConn.getConnection();
             Statement  stmt = conn.createStatement();
             ResultSet  rs   = stmt.executeQuery(sql)) {
            while (rs.next()) users.add(mapRow(rs));
        } catch (SQLException e) {
            throw new AttendanceException("Error fetching users: " + e.getMessage(), AttendanceException.DB_ERROR);
        }
        return users;
    }

    @Override
    public boolean update(User user) throws AttendanceException {
        String sql = "UPDATE users SET username=?, password=?, fullName=?, role=? WHERE id=?";
        try (Connection conn = dbConn.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getFullName());
            ps.setString(4, user.getRole().name());
            ps.setInt(5, user.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new AttendanceException("Error updating user: " + e.getMessage(), AttendanceException.DB_ERROR);
        }
    }

    @Override
    public boolean deleteById(Integer id) throws AttendanceException {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = dbConn.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new AttendanceException("Error deleting user: " + e.getMessage(), AttendanceException.DB_ERROR);
        }
    }

    /**
     * Authenticate a user by username and password.
     */
    public Optional<User> authenticate(String username, String password) throws AttendanceException {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (Connection conn = dbConn.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new AttendanceException("Authentication error: " + e.getMessage(), AttendanceException.DB_ERROR);
        }
    }

    private User mapRow(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("id"),
                rs.getString("username"),
                rs.getString("password"),
                rs.getString("fullName"),
                User.Role.valueOf(rs.getString("role"))
        );
    }
}

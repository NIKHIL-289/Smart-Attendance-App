package com.attendance.dao;

import com.attendance.exception.AttendanceException;
import com.attendance.model.Course;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO for Course entities using JDBC and PreparedStatements.
 */
public class CourseDAO implements GenericDAO<Course, Integer> {

    private final DatabaseConnection dbConn = DatabaseConnection.getInstance();

    @Override
    public Course save(Course course) throws AttendanceException {
        String sql = "INSERT INTO courses (code, name, instructor, totalClasses) VALUES (?, ?, ?, ?)";
        try (Connection conn = dbConn.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, course.getCode());
            ps.setString(2, course.getName());
            ps.setString(3, course.getInstructor());
            ps.setInt(4, course.getTotalClasses());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) course.setId(keys.getInt(1));
            }
            return course;
        } catch (SQLException e) {
            if (e.getMessage().contains("UNIQUE")) {
                throw new AttendanceException("Course code already exists.", AttendanceException.DUPLICATE_ENTRY);
            }
            throw new AttendanceException("Error saving course: " + e.getMessage(), AttendanceException.DB_ERROR);
        }
    }

    @Override
    public Optional<Course> findById(Integer id) throws AttendanceException {
        String sql = "SELECT * FROM courses WHERE id = ?";
        try (Connection conn = dbConn.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new AttendanceException("Error finding course: " + e.getMessage(), AttendanceException.DB_ERROR);
        }
    }

    @Override
    public List<Course> findAll() throws AttendanceException {
        List<Course> courses = new ArrayList<>();
        String sql = "SELECT * FROM courses ORDER BY name";
        try (Connection conn = dbConn.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs   = stmt.executeQuery(sql)) {
            while (rs.next()) courses.add(mapRow(rs));
        } catch (SQLException e) {
            throw new AttendanceException("Error fetching courses: " + e.getMessage(), AttendanceException.DB_ERROR);
        }
        return courses;
    }

    @Override
    public boolean update(Course course) throws AttendanceException {
        String sql = "UPDATE courses SET code=?, name=?, instructor=?, totalClasses=? WHERE id=?";
        try (Connection conn = dbConn.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, course.getCode());
            ps.setString(2, course.getName());
            ps.setString(3, course.getInstructor());
            ps.setInt(4, course.getTotalClasses());
            ps.setInt(5, course.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new AttendanceException("Error updating course: " + e.getMessage(), AttendanceException.DB_ERROR);
        }
    }

    @Override
    public boolean deleteById(Integer id) throws AttendanceException {
        String sql = "DELETE FROM courses WHERE id = ?";
        try (Connection conn = dbConn.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new AttendanceException("Error deleting course: " + e.getMessage(), AttendanceException.DB_ERROR);
        }
    }

    private Course mapRow(ResultSet rs) throws SQLException {
        return new Course(
                rs.getInt("id"),
                rs.getString("code"),
                rs.getString("name"),
                rs.getString("instructor"),
                rs.getInt("totalClasses")
        );
    }
}

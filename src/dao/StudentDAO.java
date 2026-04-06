package com.attendance.dao;

import com.attendance.exception.AttendanceException;
import com.attendance.model.Student;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO for Student entities using JDBC.
 * Demonstrates JOIN queries and PreparedStatements.
 */
public class StudentDAO implements GenericDAO<Student, Integer> {

    private final DatabaseConnection dbConn = DatabaseConnection.getInstance();

    // SQL with JOIN to get course name for display
    private static final String SELECT_WITH_COURSE =
            "SELECT s.*, c.name AS courseName FROM students s " +
            "JOIN courses c ON s.courseId = c.id ";

    @Override
    public Student save(Student student) throws AttendanceException {
        String sql = "INSERT INTO students (rollNumber, name, email, phone, courseId) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = dbConn.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, student.getRollNumber());
            ps.setString(2, student.getName());
            ps.setString(3, student.getEmail());
            ps.setString(4, student.getPhone());
            ps.setInt(5, student.getCourseId());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) student.setId(keys.getInt(1));
            }
            return student;
        } catch (SQLException e) {
            if (e.getMessage().contains("UNIQUE")) {
                throw new AttendanceException("Roll number already exists.", AttendanceException.DUPLICATE_ENTRY);
            }
            throw new AttendanceException("Error saving student: " + e.getMessage(), AttendanceException.DB_ERROR);
        }
    }

    @Override
    public Optional<Student> findById(Integer id) throws AttendanceException {
        String sql = SELECT_WITH_COURSE + "WHERE s.id = ?";
        try (Connection conn = dbConn.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new AttendanceException("Error finding student: " + e.getMessage(), AttendanceException.DB_ERROR);
        }
    }

    @Override
    public List<Student> findAll() throws AttendanceException {
        List<Student> students = new ArrayList<>();
        String sql = SELECT_WITH_COURSE + "ORDER BY s.name";
        try (Connection conn = dbConn.getConnection();
             Statement  stmt = conn.createStatement();
             ResultSet  rs   = stmt.executeQuery(sql)) {
            while (rs.next()) students.add(mapRow(rs));
        } catch (SQLException e) {
            throw new AttendanceException("Error fetching students: " + e.getMessage(), AttendanceException.DB_ERROR);
        }
        return students;
    }

    /** Fetch students enrolled in a specific course. */
    public List<Student> findByCourseId(int courseId) throws AttendanceException {
        List<Student> students = new ArrayList<>();
        String sql = SELECT_WITH_COURSE + "WHERE s.courseId = ? ORDER BY s.rollNumber";
        try (Connection conn = dbConn.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) students.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new AttendanceException("Error fetching students by course: " + e.getMessage(), AttendanceException.DB_ERROR);
        }
        return students;
    }

    @Override
    public boolean update(Student student) throws AttendanceException {
        String sql = "UPDATE students SET rollNumber=?, name=?, email=?, phone=?, courseId=? WHERE id=?";
        try (Connection conn = dbConn.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, student.getRollNumber());
            ps.setString(2, student.getName());
            ps.setString(3, student.getEmail());
            ps.setString(4, student.getPhone());
            ps.setInt(5, student.getCourseId());
            ps.setInt(6, student.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new AttendanceException("Error updating student: " + e.getMessage(), AttendanceException.DB_ERROR);
        }
    }

    @Override
    public boolean deleteById(Integer id) throws AttendanceException {
        String sql = "DELETE FROM students WHERE id = ?";
        try (Connection conn = dbConn.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new AttendanceException("Error deleting student: " + e.getMessage(), AttendanceException.DB_ERROR);
        }
    }

    private Student mapRow(ResultSet rs) throws SQLException {
        Student s = new Student(
                rs.getInt("id"),
                rs.getString("rollNumber"),
                rs.getString("name"),
                rs.getString("email"),
                rs.getString("phone"),
                rs.getInt("courseId")
        );
        try { s.setCourseName(rs.getString("courseName")); } catch (SQLException ignored) {}
        return s;
    }
}

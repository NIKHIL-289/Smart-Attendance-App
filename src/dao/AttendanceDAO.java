package com.attendance.dao;

import com.attendance.exception.AttendanceException;
import com.attendance.model.AttendanceRecord;

import java.sql.*;
import java.time.LocalDate;
import java.util.*;

/**
 * DAO for Attendance operations.
 *
 * Advanced Java Concepts:
 *  - JDBC Batch Processing (markBatch)
 *  - HashMap for attendance statistics
 *  - LocalDate with JDBC
 *  - Transactions (commit / rollback)
 */
public class AttendanceDAO implements GenericDAO<AttendanceRecord, Integer> {

    private final DatabaseConnection dbConn = DatabaseConnection.getInstance();

    private static final String SELECT_FULL =
            "SELECT a.*, s.name AS studentName, s.rollNumber, c.name AS courseName " +
            "FROM attendance a " +
            "JOIN students s ON a.studentId = s.id " +
            "JOIN courses  c ON a.courseId  = c.id ";

    @Override
    public AttendanceRecord save(AttendanceRecord record) throws AttendanceException {
        String sql = "INSERT OR REPLACE INTO attendance (studentId, courseId, date, status) VALUES (?, ?, ?, ?)";
        try (Connection conn = dbConn.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, record.getStudentId());
            ps.setInt(2, record.getCourseId());
            ps.setString(3, record.getDate().toString());
            ps.setString(4, record.getStatus().name());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) record.setId(keys.getInt(1));
            }
            return record;
        } catch (SQLException e) {
            throw new AttendanceException("Error saving attendance: " + e.getMessage(), AttendanceException.DB_ERROR);
        }
    }

    /**
     * Batch-insert/update attendance for a whole class on a date.
     * Uses JDBC batch processing and transactions.
     *
     * @param records list of attendance records for one course / date
     */
    public void markBatch(List<AttendanceRecord> records) throws AttendanceException {
        if (records == null || records.isEmpty()) return;
        String sql = "INSERT OR REPLACE INTO attendance (studentId, courseId, date, status) VALUES (?, ?, ?, ?)";
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            conn.setAutoCommit(false);                    // Begin transaction
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (AttendanceRecord r : records) {
                    ps.setInt(1, r.getStudentId());
                    ps.setInt(2, r.getCourseId());
                    ps.setString(3, r.getDate().toString());
                    ps.setString(4, r.getStatus().name());
                    ps.addBatch();                         // Add to batch
                }
                ps.executeBatch();                         // Execute all at once
            }
            conn.commit();                                 // Commit transaction
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { /* ignore */ }
            }
            throw new AttendanceException("Batch attendance error: " + e.getMessage(), AttendanceException.DB_ERROR);
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
            }
        }
    }

    @Override
    public Optional<AttendanceRecord> findById(Integer id) throws AttendanceException {
        String sql = SELECT_FULL + "WHERE a.id = ?";
        try (Connection conn = dbConn.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new AttendanceException("Error finding record: " + e.getMessage(), AttendanceException.DB_ERROR);
        }
    }

    @Override
    public List<AttendanceRecord> findAll() throws AttendanceException {
        List<AttendanceRecord> list = new ArrayList<>();
        String sql = SELECT_FULL + "ORDER BY a.date DESC";
        try (Connection conn = dbConn.getConnection();
             Statement  stmt = conn.createStatement();
             ResultSet  rs   = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new AttendanceException("Error fetching records: " + e.getMessage(), AttendanceException.DB_ERROR);
        }
        return list;
    }

    /** Fetch all records for a specific course on a specific date. */
    public List<AttendanceRecord> findByCourseAndDate(int courseId, LocalDate date) throws AttendanceException {
        List<AttendanceRecord> list = new ArrayList<>();
        String sql = SELECT_FULL + "WHERE a.courseId = ? AND a.date = ? ORDER BY s.rollNumber";
        try (Connection conn = dbConn.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, courseId);
            ps.setString(2, date.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new AttendanceException("Error fetching records: " + e.getMessage(), AttendanceException.DB_ERROR);
        }
        return list;
    }

    /** Fetch attendance summary for each student in a course.
     *  Returns Map<studentId, presentCount>. */
    public Map<Integer, Integer> getAttendanceSummary(int courseId) throws AttendanceException {
        Map<Integer, Integer> summary = new LinkedHashMap<>();
        String sql = "SELECT studentId, COUNT(*) AS presentCount FROM attendance " +
                     "WHERE courseId = ? AND status = 'PRESENT' GROUP BY studentId";
        try (Connection conn = dbConn.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    summary.put(rs.getInt("studentId"), rs.getInt("presentCount"));
                }
            }
        } catch (SQLException e) {
            throw new AttendanceException("Error getting summary: " + e.getMessage(), AttendanceException.DB_ERROR);
        }
        return summary;
    }

    /** Fetch all records for a specific student. */
    public List<AttendanceRecord> findByStudent(int studentId) throws AttendanceException {
        List<AttendanceRecord> list = new ArrayList<>();
        String sql = SELECT_FULL + "WHERE a.studentId = ? ORDER BY a.date DESC";
        try (Connection conn = dbConn.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new AttendanceException("Error fetching student records: " + e.getMessage(), AttendanceException.DB_ERROR);
        }
        return list;
    }

    @Override
    public boolean update(AttendanceRecord record) throws AttendanceException {
        String sql = "UPDATE attendance SET status=? WHERE id=?";
        try (Connection conn = dbConn.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, record.getStatus().name());
            ps.setInt(2, record.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new AttendanceException("Error updating record: " + e.getMessage(), AttendanceException.DB_ERROR);
        }
    }

    @Override
    public boolean deleteById(Integer id) throws AttendanceException {
        String sql = "DELETE FROM attendance WHERE id = ?";
        try (Connection conn = dbConn.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new AttendanceException("Error deleting record: " + e.getMessage(), AttendanceException.DB_ERROR);
        }
    }

    private AttendanceRecord mapRow(ResultSet rs) throws SQLException {
        AttendanceRecord r = new AttendanceRecord(
                rs.getInt("id"),
                rs.getInt("studentId"),
                rs.getInt("courseId"),
                LocalDate.parse(rs.getString("date")),
                AttendanceRecord.Status.valueOf(rs.getString("status"))
        );
        try {
            r.setStudentName(rs.getString("studentName"));
            r.setRollNumber(rs.getString("rollNumber"));
            r.setCourseName(rs.getString("courseName"));
        } catch (SQLException ignored) {}
        return r;
    }
}

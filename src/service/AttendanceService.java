package com.attendance.service;

import com.attendance.dao.AttendanceDAO;
import com.attendance.dao.CourseDAO;
import com.attendance.dao.StudentDAO;
import com.attendance.exception.AttendanceException;
import com.attendance.model.AttendanceRecord;
import com.attendance.model.Course;
import com.attendance.model.Student;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service layer containing business logic for attendance management.
 *
 * Advanced Java Concepts:
 *  - Java Stream API (filter, map, collect, groupingBy)
 *  - Lambda expressions
 *  - File I/O with BufferedWriter (CSV export)
 *  - Java Collections manipulation
 *  - Optional handling
 */
public class AttendanceService {

    private final AttendanceDAO attendanceDAO = new AttendanceDAO();
    private final StudentDAO    studentDAO    = new StudentDAO();
    private final CourseDAO     courseDAO     = new CourseDAO();

    /**
     * Mark attendance for an entire class in one batch operation.
     */
    public void markAttendance(int courseId, LocalDate date,
                               Map<Integer, AttendanceRecord.Status> studentStatusMap)
            throws AttendanceException {

        // Build records using lambda + stream
        List<AttendanceRecord> records = studentStatusMap.entrySet().stream()
                .map(entry -> {
                    AttendanceRecord rec = new AttendanceRecord();
                    rec.setStudentId(entry.getKey());
                    rec.setCourseId(courseId);
                    rec.setDate(date);
                    rec.setStatus(entry.getValue());
                    return rec;
                })
                .collect(Collectors.toList());

        attendanceDAO.markBatch(records);
    }

    /**
     * Calculate attendance percentage for a student in a course.
     *
     * @return percentage (0.0 – 100.0)
     */
    public double getAttendancePercentage(int studentId, int courseId) throws AttendanceException {
        Optional<Course> course = courseDAO.findById(courseId);
        if (course.isEmpty() || course.get().getTotalClasses() == 0) return 0.0;

        List<AttendanceRecord> records = attendanceDAO.findByStudent(studentId);

        // Stream API: filter by course and PRESENT status
        long presentCount = records.stream()
                .filter(r -> r.getCourseId() == courseId)
                .filter(r -> r.getStatus() == AttendanceRecord.Status.PRESENT)
                .count();

        return (presentCount * 100.0) / course.get().getTotalClasses();
    }

    /**
     * Get a full attendance report for all students in a course.
     * Returns list of StudentAttendanceSummary (as String[]).
     * Columns: Roll No, Name, Present, Absent, Late, Percentage
     */
    public List<String[]> getCourseAttendanceReport(int courseId) throws AttendanceException {
        List<Student> students = studentDAO.findByCourseId(courseId);
        Optional<Course> course = courseDAO.findById(courseId);
        int totalClasses = course.map(Course::getTotalClasses).orElse(1);

        List<AttendanceRecord> allRecords = attendanceDAO.findAll();

        // Group records by studentId using Streams
        Map<Integer, List<AttendanceRecord>> byStudent = allRecords.stream()
                .filter(r -> r.getCourseId() == courseId)
                .collect(Collectors.groupingBy(AttendanceRecord::getStudentId));

        List<String[]> report = new ArrayList<>();

        for (Student student : students) {
            List<AttendanceRecord> studentRecords =
                    byStudent.getOrDefault(student.getId(), Collections.emptyList());

            long present = studentRecords.stream()
                    .filter(r -> r.getStatus() == AttendanceRecord.Status.PRESENT).count();
            long absent  = studentRecords.stream()
                    .filter(r -> r.getStatus() == AttendanceRecord.Status.ABSENT).count();
            long late    = studentRecords.stream()
                    .filter(r -> r.getStatus() == AttendanceRecord.Status.LATE).count();

            double pct = totalClasses > 0 ? (present * 100.0) / totalClasses : 0.0;

            report.add(new String[]{
                    student.getRollNumber(),
                    student.getName(),
                    String.valueOf(present),
                    String.valueOf(absent),
                    String.valueOf(late),
                    String.format("%.1f%%", pct)
            });
        }

        // Sort by percentage descending using lambda
        report.sort((a, b) -> Double.compare(
                Double.parseDouble(b[5].replace("%", "")),
                Double.parseDouble(a[5].replace("%", ""))
        ));

        return report;
    }

    /**
     * Export attendance report for a course to a CSV file.
     * Demonstrates File I/O with BufferedWriter.
     *
     * @param courseId  the course to export
     * @param filePath  destination CSV file path
     */
    public void exportToCSV(int courseId, String filePath) throws AttendanceException {
        Optional<Course> course = courseDAO.findById(courseId);
        String courseName = course.map(Course::getName).orElse("Unknown");

        List<String[]> report = getCourseAttendanceReport(courseId);

        // File I/O with try-with-resources (auto-closes BufferedWriter)
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write("Smart Attendance Management System – Export");
            writer.newLine();
            writer.write("Course: " + courseName);
            writer.newLine();
            writer.write("Exported on: " + LocalDate.now());
            writer.newLine();
            writer.newLine();
            writer.write("Roll No,Name,Present,Absent,Late,Percentage");
            writer.newLine();

            for (String[] row : report) {
                writer.write(String.join(",", row));
                writer.newLine();
            }

            writer.flush();
        } catch (IOException e) {
            throw new AttendanceException("CSV export failed: " + e.getMessage(), AttendanceException.DB_ERROR);
        }
    }

    /**
     * Identify students with attendance below a given threshold.
     * Uses Stream's filter + collect.
     */
    public List<Student> getLowAttendanceStudents(int courseId, double threshold)
            throws AttendanceException {

        List<Student> students = studentDAO.findByCourseId(courseId);

        return students.stream()
                .filter(s -> {
                    try {
                        return getAttendancePercentage(s.getId(), courseId) < threshold;
                    } catch (AttendanceException e) {
                        return false;
                    }
                })
                .collect(Collectors.toList());
    }

    // Delegate simple CRUD to DAOs
    public AttendanceDAO getAttendanceDAO() { return attendanceDAO; }
    public StudentDAO    getStudentDAO()    { return studentDAO; }
    public CourseDAO     getCourseDAO()     { return courseDAO; }
}

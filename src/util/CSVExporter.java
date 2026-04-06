package com.attendance.util;

import com.attendance.exception.AttendanceException;
import com.attendance.model.AttendanceRecord;
import com.attendance.model.Student;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * CSVExporter — exports attendance data to CSV files.
 *
 * Advanced Java concepts demonstrated:
 *  - Java NIO (java.nio.file.Files, Path, Paths) — modern file I/O
 *  - BufferedWriter for efficient character-stream writing
 *  - Try-with-resources (AutoCloseable)
 *  - StringBuilder for efficient string building
 *  - FileWriter with charset specification
 */
public class CSVExporter {

    private static final String EXPORT_DIR = "exports/";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    static {
        // Ensure export directory exists using NIO
        try {
            Files.createDirectories(Paths.get(EXPORT_DIR));
        } catch (IOException e) {
            System.err.println("Warning: Could not create exports dir: " + e.getMessage());
        }
    }

    /**
     * Export attendance records for a date to CSV.
     * Demonstrates BufferedWriter + try-with-resources.
     */
    public static String exportDailyAttendance(List<AttendanceRecord> records,
                                                List<Student> students,
                                                String courseId,
                                                LocalDate date) throws AttendanceException {
        String filename = EXPORT_DIR + "attendance_" + courseId + "_" + date + ".csv";
        Path path = Paths.get(filename);

        // Build lookup map for student names
        Map<Integer, Student> studentMap = new java.util.HashMap<>();
        students.forEach(s -> studentMap.put(s.getId(), s));

        try (BufferedWriter bw = Files.newBufferedWriter(path,
                java.nio.charset.StandardCharsets.UTF_8)) {

            // Write CSV header
            bw.write("Roll Number,Student Name,Department,Status,Remarks,Date");
            bw.newLine();

            // Write each record using StringBuilder
            for (AttendanceRecord rec : records) {
                Student s = studentMap.get(rec.getStudentId());
                StringBuilder sb = new StringBuilder();
                sb.append(s != null ? escape(s.getRollNumber()) : "Unknown").append(",");
                sb.append(s != null ? escape(s.getName())       : "Unknown").append(",");
                sb.append(rec.getStatus().name()).append(",");
                sb.append(date.format(DATE_FMT));
                bw.write(sb.toString());
                bw.newLine();
            }
            bw.flush();
        } catch (IOException e) {
            throw new AttendanceException("Failed to export CSV: " + e.getMessage(), e);
        }

        return filename;
    }

    /**
     * Export course-wide attendance summary to CSV.
     */
    public static String exportCourseSummary(Map<String, Double> summary,
                                              String courseId) throws AttendanceException {
        String filename = EXPORT_DIR + "summary_" + courseId + "_" +
                          LocalDate.now().format(DATE_FMT) + ".csv";
        Path path = Paths.get(filename);

        try (BufferedWriter bw = Files.newBufferedWriter(path,
                java.nio.charset.StandardCharsets.UTF_8)) {
            bw.write("Student,Attendance %,Status");
            bw.newLine();
            for (Map.Entry<String, Double> entry : summary.entrySet()) {
                double pct = entry.getValue();
                String status = pct >= 75 ? "OK" : "BELOW THRESHOLD";
                bw.write(escape(entry.getKey()) + "," +
                         String.format("%.2f", pct) + "," + status);
                bw.newLine();
            }
            bw.flush();
        } catch (IOException e) {
            throw new AttendanceException("Failed to export summary CSV: " + e.getMessage(), e);
        }

        return filename;
    }

    /** Escapes commas and quotes inside CSV values. */
    private static String escape(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}

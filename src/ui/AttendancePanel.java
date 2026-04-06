package com.attendance.ui;

import com.attendance.dao.CourseDAO;
import com.attendance.dao.StudentDAO;
import com.attendance.exception.AttendanceException;
import com.attendance.model.AttendanceRecord;
import com.attendance.model.Course;
import com.attendance.model.Student;
import com.attendance.model.User;
import com.attendance.service.AttendanceService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AttendancePanel extends JPanel {

    private final AttendanceService attendanceService = new AttendanceService();
    private final CourseDAO courseDAO = new CourseDAO();
    private final StudentDAO studentDAO = new StudentDAO();

    private JComboBox<Course> courseCombo;
    private JTextField dateField;
    private DefaultTableModel tableModel;
    private JTable table;
    private List<Student> currentStudents;

    public AttendancePanel(User currentUser) {
        setLayout(new BorderLayout(8, 8));
        setBorder(new EmptyBorder(12, 12, 12, 12));
        initUI();
    }

    private void initUI() {
        // Top Control Panel
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        
        courseCombo = new JComboBox<>();
        try {
            courseDAO.findAll().forEach(courseCombo::addItem);
        } catch (AttendanceException e) {
            e.printStackTrace();
        }

        dateField = new JTextField(LocalDate.now().toString(), 10);
        JButton loadBtn = new JButton("Load Students");
        
        topPanel.add(new JLabel("Select Course:"));
        topPanel.add(courseCombo);
        topPanel.add(new JLabel("Date (YYYY-MM-DD):"));
        topPanel.add(dateField);
        topPanel.add(loadBtn);

        // Attendance Table Setup
        String[] columns = {"Student ID", "Roll No", "Name", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 3; // Only Status is editable
            }
        };
        table = new JTable(tableModel);
        table.setRowHeight(25);
        
        // Add dropdown to the Status column
        JComboBox<AttendanceRecord.Status> statusEditor = new JComboBox<>(AttendanceRecord.Status.values());
        table.getColumnModel().getColumn(3).setCellEditor(new DefaultCellEditor(statusEditor));

        // Bottom Save Panel
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveBtn = new JButton("✅ Save Attendance");
        saveBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        bottomPanel.add(saveBtn);

        loadBtn.addActionListener(e -> loadClassList());
        saveBtn.addActionListener(e -> saveAttendanceBatch());

        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void loadClassList() {
        Course selectedCourse = (Course) courseCombo.getSelectedItem();
        if (selectedCourse == null) return;

        try {
            currentStudents = studentDAO.findByCourseId(selectedCourse.getId());
            tableModel.setRowCount(0);
            
            // Default all students to PRESENT initially
            for (Student s : currentStudents) {
                tableModel.addRow(new Object[]{
                        s.getId(), s.getRollNumber(), s.getName(), AttendanceRecord.Status.PRESENT
                });
            }
        } catch (AttendanceException ex) {
            JOptionPane.showMessageDialog(this, "Error loading students: " + ex.getMessage());
        }
    }

    private void saveAttendanceBatch() {
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No students loaded.");
            return;
        }

        Course selectedCourse = (Course) courseCombo.getSelectedItem();
        LocalDate date;
        try {
            date = LocalDate.parse(dateField.getText().trim());
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Invalid date format. Use YYYY-MM-DD");
            return;
        }

        Map<Integer, AttendanceRecord.Status> statusMap = new HashMap<>();
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            int studentId = (int) tableModel.getValueAt(i, 0);
            AttendanceRecord.Status status = (AttendanceRecord.Status) tableModel.getValueAt(i, 3);
            statusMap.put(studentId, status);
        }

        try {
            attendanceService.markAttendance(selectedCourse.getId(), date, statusMap);
            JOptionPane.showMessageDialog(this, "Attendance successfully saved for " + date.toString());
        } catch (AttendanceException ex) {
            JOptionPane.showMessageDialog(this, "Failed to save attendance: " + ex.getMessage());
        }
    }
}

package com.attendance.ui;

import com.attendance.dao.CourseDAO;
import com.attendance.exception.AttendanceException;
import com.attendance.model.Course;
import com.attendance.model.User;
import com.attendance.service.AttendanceService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.List;

public class ReportPanel extends JPanel {

    private final AttendanceService attendanceService = new AttendanceService();
    private final CourseDAO courseDAO = new CourseDAO();

    private JComboBox<Course> courseCombo;
    private DefaultTableModel tableModel;
    private JTable table;

    public ReportPanel(User currentUser) {
        setLayout(new BorderLayout(8, 8));
        setBorder(new EmptyBorder(12, 12, 12, 12));
        initUI();
    }

    private void initUI() {
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        
        courseCombo = new JComboBox<>();
        try {
            courseDAO.findAll().forEach(courseCombo::addItem);
        } catch (AttendanceException e) {
            e.printStackTrace();
        }

        JButton generateBtn = new JButton("📊 Generate Report");
        JButton exportBtn = new JButton("💾 Export to CSV");
        
        topPanel.add(new JLabel("Select Course:"));
        topPanel.add(courseCombo);
        topPanel.add(generateBtn);
        topPanel.add(exportBtn);

        String[] columns = {"Roll No", "Name", "Present", "Absent", "Late", "Percentage"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        table = new JTable(tableModel);
        table.setRowHeight(24);
        table.setAutoCreateRowSorter(true);

        generateBtn.addActionListener(e -> generateReport());
        exportBtn.addActionListener(e -> exportReport());

        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    private void generateReport() {
        Course selectedCourse = (Course) courseCombo.getSelectedItem();
        if (selectedCourse == null) return;

        try {
            List<String[]> reportData = attendanceService.getCourseAttendanceReport(selectedCourse.getId());
            tableModel.setRowCount(0); // Clear old data
            
            for (String[] row : reportData) {
                tableModel.addRow(row);
            }
        } catch (AttendanceException ex) {
            JOptionPane.showMessageDialog(this, "Error generating report: " + ex.getMessage());
        }
    }

    private void exportReport() {
        Course selectedCourse = (Course) courseCombo.getSelectedItem();
        if (selectedCourse == null || tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Please generate a report first.");
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Report as CSV");
        fileChooser.setSelectedFile(new File("AttendanceReport_" + selectedCourse.getCode() + ".csv"));

        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            try {
                attendanceService.exportToCSV(selectedCourse.getId(), fileToSave.getAbsolutePath());
                JOptionPane.showMessageDialog(this, "Successfully exported to:\n" + fileToSave.getAbsolutePath());
            } catch (AttendanceException ex) {
                JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage());
            }
        }
    }
}
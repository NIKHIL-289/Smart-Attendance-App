package com.attendance.ui;

import com.attendance.dao.CourseDAO;
import com.attendance.exception.AttendanceException;
import com.attendance.model.Course;
import com.attendance.model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class CoursePanel extends JPanel {

    private final User currentUser;
    private final CourseDAO courseDAO = new CourseDAO();

    private DefaultTableModel tableModel;
    private JTable table;
    private List<Course> courseList;

    private static final String[] COLUMNS = {"ID", "Course Code", "Course Name", "Instructor", "Total Classes"};

    public CoursePanel(User currentUser) {
        this.currentUser = currentUser;
        setLayout(new BorderLayout(8, 8));
        setBorder(new EmptyBorder(12, 12, 12, 12));
        initUI();
        loadData();
    }

    private void initUI() {
        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setRowHeight(24);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        table.getColumnModel().getColumn(0).setMaxWidth(50);

        JScrollPane scroll = new JScrollPane(table);

        JButton addBtn = new JButton("➕ Add Course");
        JButton editBtn = new JButton("✏️ Edit");
        JButton deleteBtn = new JButton("🗑️ Delete");
        JButton refreshBtn = new JButton("🔄 Refresh");

        addBtn.addActionListener(e -> showCourseDialog(null));
        editBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Select a course first.");
                return;
            }
            int id = (int) tableModel.getValueAt(table.convertRowIndexToModel(row), 0);
            courseList.stream().filter(c -> c.getId() == id).findFirst().ifPresent(this::showCourseDialog);
        });
        deleteBtn.addActionListener(e -> deleteSelected());
        refreshBtn.addActionListener(e -> loadData());

        // Restrict modifications to ADMIN role
        if (currentUser.getRole() != User.Role.ADMIN) {
            addBtn.setEnabled(false);
            editBtn.setEnabled(false);
            deleteBtn.setEnabled(false);
        }

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        btnPanel.add(refreshBtn);
        btnPanel.add(addBtn);
        btnPanel.add(editBtn);
        btnPanel.add(deleteBtn);

        add(btnPanel, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
    }

    private void loadData() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                courseList = courseDAO.findAll();
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    refreshTable();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(CoursePanel.this, "Error loading courses: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        for (Course c : courseList) {
            tableModel.addRow(new Object[]{
                    c.getId(), c.getCode(), c.getName(), c.getInstructor(), c.getTotalClasses()
            });
        }
    }

    private void showCourseDialog(Course existing) {
        boolean isEdit = (existing != null);
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                isEdit ? "Edit Course" : "Add Course", true);
        dialog.setLayout(new BorderLayout(8, 8));

        JTextField codeField = new JTextField(existing != null ? existing.getCode() : "");
        JTextField nameField = new JTextField(existing != null ? existing.getName() : "");
        JTextField instField = new JTextField(existing != null ? existing.getInstructor() : "");
        JSpinner classSpinner = new JSpinner(new SpinnerNumberModel(existing != null ? existing.getTotalClasses() : 0, 0, 1000, 1));

        JPanel form = new JPanel(new GridLayout(4, 2, 6, 6));
        form.setBorder(new EmptyBorder(12, 12, 6, 12));
        form.add(new JLabel("Course Code:")); form.add(codeField);
        form.add(new JLabel("Course Name:")); form.add(nameField);
        form.add(new JLabel("Instructor:"));  form.add(instField);
        form.add(new JLabel("Total Classes:")); form.add(classSpinner);

        JButton saveBtn = new JButton(isEdit ? "Update" : "Save");
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());
        
        saveBtn.addActionListener(e -> {
            String code = codeField.getText().trim();
            String name = nameField.getText().trim();
            String inst = instField.getText().trim();
            
            if (code.isEmpty() || name.isEmpty() || inst.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "All fields are required.");
                return;
            }
            
            Course c = isEdit ? existing : new Course();
            c.setCode(code);
            c.setName(name);
            c.setInstructor(inst);
            c.setTotalClasses((Integer) classSpinner.getValue());
            
            try {
                if (isEdit) courseDAO.update(c); else courseDAO.save(c);
                dialog.dispose();
                loadData();
            } catch (AttendanceException ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage());
            }
        });

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btns.add(cancelBtn); btns.add(saveBtn);

        dialog.add(form, BorderLayout.CENTER);
        dialog.add(btns, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        int id = (int) tableModel.getValueAt(table.convertRowIndexToModel(row), 0);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete this course? All associated students and attendance will be removed (Cascade Delete).",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;
        try {
            courseDAO.deleteById(id);
            loadData();
        } catch (AttendanceException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }
}
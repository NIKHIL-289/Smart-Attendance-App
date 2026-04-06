package com.attendance.ui;

import com.attendance.dao.CourseDAO;
import com.attendance.dao.StudentDAO;
import com.attendance.exception.AttendanceException;
import com.attendance.model.Course;
import com.attendance.model.Student;
import com.attendance.model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

/**
 * Panel for Student CRUD operations.
 *
 * Advanced Java Concepts:
 *  - JTable with DefaultTableModel
 *  - TableRowSorter for column sorting
 *  - JComboBox with generic type parameter
 *  - SwingWorker for async DB calls
 */
public class StudentPanel extends JPanel {

    private final User currentUser;
    private final StudentDAO studentDAO = new StudentDAO();
    private final CourseDAO  courseDAO  = new CourseDAO();

    private DefaultTableModel tableModel;
    private JTable            table;
    private List<Student>     studentList;
    private List<Course>      courseList;

    private static final String[] COLUMNS = {"ID", "Roll No", "Name", "Email", "Phone", "Course"};

    public StudentPanel(User currentUser) {
        this.currentUser = currentUser;
        setLayout(new BorderLayout(8, 8));
        setBorder(new EmptyBorder(12, 12, 12, 12));
        initUI();
        loadData();
    }

    private void initUI() {
        // --- Table ---
        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(24);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);                          // sorting
        table.getColumnModel().getColumn(0).setMaxWidth(50);         // narrow ID col

        JScrollPane scroll = new JScrollPane(table);

        // --- Search bar ---
        JTextField searchField = new JTextField(16);
        searchField.setToolTipText("Search by name or roll number…");
        JButton searchBtn = new JButton("🔍 Search");
        searchBtn.addActionListener(e -> filterTable(searchField.getText().trim()));
        searchField.addActionListener(e -> filterTable(searchField.getText().trim()));

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(searchBtn);

        // --- Buttons ---
        JButton addBtn    = new JButton("➕ Add");
        JButton editBtn   = new JButton("✏️ Edit");
        JButton deleteBtn = new JButton("🗑️ Delete");
        JButton refreshBtn= new JButton("🔄 Refresh");

        addBtn.addActionListener(e -> showStudentDialog(null));
        editBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Select a student first."); return; }
            int id = (int) tableModel.getValueAt(table.convertRowIndexToModel(row), 0);
            studentList.stream().filter(s -> s.getId() == id).findFirst()
                    .ifPresent(this::showStudentDialog);
        });
        deleteBtn.addActionListener(e -> deleteSelected());
        refreshBtn.addActionListener(e -> loadData());

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

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.add(searchPanel, BorderLayout.WEST);
        topBar.add(btnPanel,    BorderLayout.EAST);

        add(topBar, BorderLayout.NORTH);
        add(scroll,  BorderLayout.CENTER);
    }

    private void loadData() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override protected Void doInBackground() throws Exception {
                studentList = studentDAO.findAll();
                courseList  = courseDAO.findAll();
                return null;
            }
            @Override protected void done() {
                try {
                    get();
                    refreshTable(studentList);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(StudentPanel.this, "Error loading data: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void refreshTable(List<Student> list) {
        tableModel.setRowCount(0);
        for (Student s : list) {
            tableModel.addRow(new Object[]{
                    s.getId(), s.getRollNumber(), s.getName(),
                    s.getEmail(), s.getPhone(), s.getCourseName()
            });
        }
    }

    private void filterTable(String text) {
        if (text.isEmpty()) { refreshTable(studentList); return; }
        String lower = text.toLowerCase();
        List<Student> filtered = studentList.stream()
                .filter(s -> s.getName().toLowerCase().contains(lower)
                          || s.getRollNumber().toLowerCase().contains(lower))
                .toList();
        refreshTable(filtered);
    }

    private void showStudentDialog(Student existing) {
        boolean isEdit = (existing != null);
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                isEdit ? "Edit Student" : "Add Student", true);
        dialog.setLayout(new BorderLayout(8, 8));

        JTextField rollField  = new JTextField(existing != null ? existing.getRollNumber() : "");
        JTextField nameField  = new JTextField(existing != null ? existing.getName() : "");
        JTextField emailField = new JTextField(existing != null ? existing.getEmail()  : "");
        JTextField phoneField = new JTextField(existing != null ? existing.getPhone()  : "");
        JComboBox<Course> courseBox = new JComboBox<>();
        if (courseList != null) courseList.forEach(courseBox::addItem);
        if (existing != null) {
            for (int i = 0; i < courseBox.getItemCount(); i++) {
                if (courseBox.getItemAt(i).getId() == existing.getCourseId()) {
                    courseBox.setSelectedIndex(i); break;
                }
            }
        }

        JPanel form = new JPanel(new GridLayout(5, 2, 6, 6));
        form.setBorder(new EmptyBorder(12, 12, 6, 12));
        form.add(new JLabel("Roll Number:")); form.add(rollField);
        form.add(new JLabel("Name:"));        form.add(nameField);
        form.add(new JLabel("Email:"));       form.add(emailField);
        form.add(new JLabel("Phone:"));       form.add(phoneField);
        form.add(new JLabel("Course:"));      form.add(courseBox);

        JButton saveBtn   = new JButton(isEdit ? "Update" : "Save");
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());
        saveBtn.addActionListener(e -> {
            String roll  = rollField.getText().trim();
            String name  = nameField.getText().trim();
            if (roll.isEmpty() || name.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Roll number and name are required.");
                return;
            }
            Course selectedCourse = (Course) courseBox.getSelectedItem();
            if (selectedCourse == null) {
                JOptionPane.showMessageDialog(dialog, "Please add a course first.");
                return;
            }
            Student s = isEdit ? existing : new Student();
            s.setRollNumber(roll);
            s.setName(name);
            s.setEmail(emailField.getText().trim());
            s.setPhone(phoneField.getText().trim());
            s.setCourseId(selectedCourse.getId());
            try {
                if (isEdit) studentDAO.update(s); else studentDAO.save(s);
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
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a student first."); return; }
        int id = (int) tableModel.getValueAt(table.convertRowIndexToModel(row), 0);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete this student? All attendance records will also be removed.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;
        try {
            studentDAO.deleteById(id);
            loadData();
        } catch (AttendanceException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }
}

package com.attendance.ui;

import com.attendance.dao.AttendanceDAO;
import com.attendance.dao.CourseDAO;
import com.attendance.dao.StudentDAO;
import com.attendance.model.AttendanceRecord;
import com.attendance.model.User;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Dashboard panel showing quick-glance statistics.
 * Uses SwingWorker to load DB stats without blocking the EDT.
 */
public class DashboardPanel extends JPanel {

    private final User currentUser;
    private final JLabel lblStudents  = new JLabel("–");
    private final JLabel lblCourses   = new JLabel("–");
    private final JLabel lblToday     = new JLabel("–");
    private final JLabel lblDate      = new JLabel();

    public DashboardPanel(User currentUser) {
        this.currentUser = currentUser;
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(20, 20, 20, 20));
        setBackground(new Color(245, 248, 255));
        initUI();
        loadStats();
    }

    private void initUI() {
        // Welcome header
        JLabel welcome = new JLabel("Welcome, " + currentUser.getFullName() + "!");
        welcome.setFont(new Font("Segoe UI", Font.BOLD, 20));
        welcome.setForeground(new Color(30, 90, 160));

        LocalDate today = LocalDate.now();
        lblDate.setText(today.format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy")));
        lblDate.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblDate.setForeground(Color.GRAY);

        JPanel headerPanel = new JPanel(new GridLayout(2, 1, 0, 4));
        headerPanel.setBackground(getBackground());
        headerPanel.add(welcome);
        headerPanel.add(lblDate);

        // Stats cards
        JPanel cardsPanel = new JPanel(new GridLayout(1, 3, 16, 0));
        cardsPanel.setBackground(getBackground());
        cardsPanel.setBorder(new EmptyBorder(20, 0, 0, 0));

        cardsPanel.add(buildCard("Total Students", lblStudents, new Color(52, 152, 219)));
        cardsPanel.add(buildCard("Total Courses",  lblCourses,  new Color(39, 174, 96)));
        cardsPanel.add(buildCard("Today's Records", lblToday,   new Color(155, 89, 182)));

        JLabel info = new JLabel(
            "<html><b>Quick Guide:</b> Use the tabs above to manage Students, Courses, "
          + "mark Attendance, or view Reports.</html>",
            SwingConstants.CENTER
        );
        info.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        info.setForeground(Color.DARK_GRAY);
        info.setBorder(new EmptyBorder(20, 0, 0, 0));

        add(headerPanel, BorderLayout.NORTH);
        add(cardsPanel,  BorderLayout.CENTER);
        add(info,        BorderLayout.SOUTH);
    }

    private JPanel buildCard(String title, JLabel valueLabel, Color color) {
        JPanel card = new JPanel(new GridLayout(2, 1, 0, 8));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color, 2, true),
                new EmptyBorder(16, 20, 16, 20)
        ));
        card.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        titleLabel.setForeground(Color.DARK_GRAY);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        valueLabel.setForeground(color);
        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);

        card.add(titleLabel);
        card.add(valueLabel);
        return card;
    }

    private void loadStats() {
        SwingWorker<int[], Void> worker = new SwingWorker<>() {
            @Override
            protected int[] doInBackground() throws Exception {
                int students = new StudentDAO().findAll().size();
                int courses  = new CourseDAO().findAll().size();
                List<AttendanceRecord> todayRecords =
                        new AttendanceDAO().findAll().stream()
                                .filter(r -> r.getDate().equals(LocalDate.now()))
                                .toList();
                return new int[]{ students, courses, todayRecords.size() };
            }

            @Override
            protected void done() {
                try {
                    int[] stats = get();
                    lblStudents.setText(String.valueOf(stats[0]));
                    lblCourses.setText(String.valueOf(stats[1]));
                    lblToday.setText(String.valueOf(stats[2]));
                } catch (Exception ignored) {}
            }
        };
        worker.execute();
    }
}

package com.attendance.ui;

import com.attendance.model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Main application window shown after successful login.
 * Uses JTabbedPane to host all feature panels.
 *
 * Advanced Java Concepts:
 *  - JTabbedPane, JMenuBar
 *  - Inner class WindowAdapter for clean shutdown
 *  - Runtime user-role–based tab visibility
 */
public class MainFrame extends JFrame {

    private final User currentUser;

    public MainFrame(User currentUser) {
        this.currentUser = currentUser;
        setTitle("Smart Attendance Management – " + currentUser.getFullName());
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setMinimumSize(new Dimension(900, 620));
        initUI();
        pack();
        setLocationRelativeTo(null);

        // Clean shutdown
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                int choice = JOptionPane.showConfirmDialog(
                        MainFrame.this,
                        "Exit the application?",
                        "Confirm Exit",
                        JOptionPane.YES_NO_OPTION
                );
                if (choice == JOptionPane.YES_OPTION) {
                    com.attendance.dao.DatabaseConnection.getInstance().closeConnection();
                    System.exit(0);
                }
            }
        });
    }

    private void initUI() {
        // --- Status bar ---
        JLabel statusBar = new JLabel("  Logged in as: " + currentUser.getFullName()
                + "  |  Role: " + currentUser.getRole());
        statusBar.setBorder(new EmptyBorder(4, 6, 4, 6));
        statusBar.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        statusBar.setForeground(Color.DARK_GRAY);
        statusBar.setBackground(new Color(230, 235, 245));
        statusBar.setOpaque(true);

        // --- Tabs ---
        JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP);
        tabs.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        tabs.addTab("📋 Dashboard",  new DashboardPanel(currentUser));
        tabs.addTab("🎓 Students",   new StudentPanel(currentUser));
        tabs.addTab("📚 Courses",    new CoursePanel(currentUser));
        tabs.addTab("✅ Attendance", new AttendancePanel(currentUser));
        tabs.addTab("📊 Reports",    new ReportPanel(currentUser));

        // Restrict Courses tab to Admin only
        if (currentUser.getRole() != User.Role.ADMIN) {
            tabs.setEnabledAt(2, false);
            tabs.setToolTipTextAt(2, "Admin access only");
        }

        // --- Menu bar ---
        setJMenuBar(buildMenuBar());

        // --- Layout ---
        setLayout(new BorderLayout());
        add(tabs, BorderLayout.CENTER);
        add(statusBar, BorderLayout.SOUTH);
    }

    private JMenuBar buildMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        JMenuItem logoutItem = new JMenuItem("Logout");
        JMenuItem exitItem   = new JMenuItem("Exit");

        logoutItem.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });
        exitItem.addActionListener(e -> {
            com.attendance.dao.DatabaseConnection.getInstance().closeConnection();
            System.exit(0);
        });

        fileMenu.add(logoutItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Smart Attendance Management System\nVersion 1.0\n\nAdvanced Java Project",
                "About", JOptionPane.INFORMATION_MESSAGE));
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(helpMenu);
        return menuBar;
    }
}

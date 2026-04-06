package com.attendance.ui;

import com.attendance.dao.UserDAO;
import com.attendance.model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Optional;

/**
 * Login window for the Smart Attendance Management System.
 *
 * Advanced Java Concepts:
 *  - Java Swing (JFrame, JPanel, JTextField, JPasswordField)
 *  - Event handling with ActionListeners (anonymous inner classes / lambdas)
 *  - GridBagLayout for form layout
 */
public class LoginFrame extends JFrame {

    private final JTextField     usernameField = new JTextField(18);
    private final JPasswordField passwordField = new JPasswordField(18);
    private final JButton        loginButton   = new JButton("Login");
    private final JLabel         statusLabel   = new JLabel(" ");

    private final UserDAO userDAO = new UserDAO();

    public LoginFrame() {
        setTitle("Smart Attendance Management – Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        initUI();
        pack();
        setLocationRelativeTo(null); // center on screen
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(new EmptyBorder(20, 30, 20, 30));
        root.setBackground(new Color(245, 248, 255));

        // --- Header ---
        JLabel title    = new JLabel("Smart Attendance", SwingConstants.CENTER);
        JLabel subtitle = new JLabel("Management System", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(new Color(30, 90, 160));
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(Color.GRAY);

        JPanel header = new JPanel(new GridLayout(2, 1, 0, 2));
        header.setBackground(root.getBackground());
        header.add(title);
        header.add(subtitle);
        header.setBorder(new EmptyBorder(0, 0, 20, 0));
        root.add(header, BorderLayout.NORTH);

        // --- Form ---
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(root.getBackground());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill   = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        form.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        form.add(usernameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        form.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        form.add(passwordField, gbc);

        root.add(form, BorderLayout.CENTER);

        // --- Button & Status ---
        JPanel south = new JPanel(new GridLayout(2, 1, 0, 4));
        south.setBackground(root.getBackground());
        south.setBorder(new EmptyBorder(12, 0, 0, 0));

        loginButton.setBackground(new Color(30, 90, 160));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 13));

        statusLabel.setForeground(Color.RED);
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);

        south.add(loginButton);
        south.add(statusLabel);
        root.add(south, BorderLayout.SOUTH);

        // Hint label
        JLabel hint = new JLabel("Default: admin / admin123", SwingConstants.CENTER);
        hint.setFont(new Font("Segoe UI", Font.ITALIC, 10));
        hint.setForeground(Color.GRAY);
        south.add(hint);

        // --- Event Listeners (lambda) ---
        loginButton.addActionListener(e -> handleLogin());
        // Allow pressing Enter in password field
        passwordField.addActionListener(e -> handleLogin());

        add(root);
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Please enter username and password.");
            return;
        }

        loginButton.setEnabled(false);
        statusLabel.setForeground(Color.GRAY);
        statusLabel.setText("Authenticating…");

        // Run DB call on a background thread to keep UI responsive (SwingWorker)
        SwingWorker<Optional<User>, Void> worker = new SwingWorker<>() {
            @Override
            protected Optional<User> doInBackground() throws Exception {
                return userDAO.authenticate(username, password);
            }

            @Override
            protected void done() {
                try {
                    Optional<User> result = get();
                    if (result.isPresent()) {
                        dispose();
                        new MainFrame(result.get()).setVisible(true);
                    } else {
                        statusLabel.setForeground(Color.RED);
                        statusLabel.setText("Invalid username or password.");
                        loginButton.setEnabled(true);
                        passwordField.setText("");
                    }
                } catch (Exception ex) {
                    statusLabel.setForeground(Color.RED);
                    statusLabel.setText("Error: " + ex.getMessage());
                    loginButton.setEnabled(true);
                }
            }
        };
        worker.execute();
    }
}

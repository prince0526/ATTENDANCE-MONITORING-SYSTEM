package src;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;
import java.text.SimpleDateFormat;

public class AttendanceSystem extends JFrame {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/attendance_system";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "titamaver";

    private JTextField empNumberField, adminUsernameField;
    private JPasswordField adminPasswordField;
    private JButton timeInButton, timeOutButton, viewAttendanceButton, adminLoginButton, eraseRecordsButton;
    private JTable attendanceTable;
    private DefaultTableModel tableModel;

    private Connection conn;
    private boolean isAdminLoggedIn = false;

    public AttendanceSystem() {
        setTitle("Attendance Monitoring System");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        try {
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database connection failed!", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            System.exit(1);
        }

        JTabbedPane tabbedPane = new JTabbedPane();

        JPanel employeePanel = new JPanel(new GridLayout(3, 2, 10, 10));
        employeePanel.setBorder(BorderFactory.createTitledBorder("Employee Attendance"));

        employeePanel.add(new JLabel("Employee Number:"));
        empNumberField = new JTextField();
        employeePanel.add(empNumberField);

        timeInButton = new JButton("Time In");
        employeePanel.add(timeInButton);

        timeOutButton = new JButton("Time Out");
        employeePanel.add(timeOutButton);

        tabbedPane.add("Employee", employeePanel);

        JPanel adminPanel = new JPanel(new BorderLayout(10, 10));
        JPanel loginPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        loginPanel.setBorder(BorderFactory.createTitledBorder("Admin Login"));

        loginPanel.add(new JLabel("Username:"));
        adminUsernameField = new JTextField();
        loginPanel.add(adminUsernameField);

        loginPanel.add(new JLabel("Password:"));
        adminPasswordField = new JPasswordField();
        loginPanel.add(adminPasswordField);

        adminLoginButton = new JButton("Login as Admin");
        loginPanel.add(adminLoginButton);

        adminPanel.add(loginPanel, BorderLayout.NORTH);

        JPanel adminControlsPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        viewAttendanceButton = new JButton("View Attendance");
        viewAttendanceButton.setEnabled(false);
        eraseRecordsButton = new JButton("Erase All Records");
        eraseRecordsButton.setEnabled(false);

        adminControlsPanel.add(viewAttendanceButton);
        adminControlsPanel.add(eraseRecordsButton);
        adminPanel.add(adminControlsPanel, BorderLayout.SOUTH);

        tabbedPane.add("Admin", adminPanel);

        tableModel = new DefaultTableModel(new String[]{"Employee Number", "Date", "Time In", "Time Out"}, 0);
        attendanceTable = new JTable(tableModel);
        JScrollPane tableScrollPane = new JScrollPane(attendanceTable);
        adminPanel.add(tableScrollPane, BorderLayout.CENTER);
        tableScrollPane.setVisible(false);

        getContentPane().add(tabbedPane);

        timeInButton.addActionListener(this::timeInAction);
        timeOutButton.addActionListener(this::timeOutAction);
        adminLoginButton.addActionListener(this::adminLoginAction);
        viewAttendanceButton.addActionListener(e -> {
            tableScrollPane.setVisible(true);
            viewAttendanceAction();
        });
        eraseRecordsButton.addActionListener(this::eraseRecordsAction);
    }

    private void timeInAction(ActionEvent e) {
        String empNumber = empNumberField.getText().trim();
        String date = new SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());

        if (empNumber.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter your employee number.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            String timeIn = new SimpleDateFormat("HH:mm:ss").format(new java.util.Date());
            String query = "INSERT INTO attendance (employee_number, date, time_in) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE time_in = ?";
            PreparedStatement pst = conn.prepareStatement(query);
            pst.setString(1, empNumber);
            pst.setString(2, date);
            pst.setString(3, timeIn);
            pst.setString(4, timeIn);
            pst.executeUpdate();
            JOptionPane.showMessageDialog(this, "Time In recorded successfully!");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error recording Time In.", "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void timeOutAction(ActionEvent e) {
        String empNumber = empNumberField.getText().trim();
        String date = new SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());

        if (empNumber.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter your employee number.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            String timeOut = new SimpleDateFormat("HH:mm:ss").format(new java.util.Date());
            String query = "UPDATE attendance SET time_out = ? WHERE employee_number = ? AND date = ?";
            PreparedStatement pst = conn.prepareStatement(query);
            pst.setString(1, timeOut);
            pst.setString(2, empNumber);
            pst.setString(3, date);
            int rowsAffected = pst.executeUpdate();

            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Time Out recorded successfully!");
            } else {
                JOptionPane.showMessageDialog(this, "No Time In record found for this date.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error recording Time Out.", "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void adminLoginAction(ActionEvent e) {
        String username = adminUsernameField.getText().trim();
        String password = new String(adminPasswordField.getPassword());

        try {
            String query = "SELECT * FROM admins WHERE username = ? AND password = ?";
            PreparedStatement pst = conn.prepareStatement(query);
            pst.setString(1, username);
            pst.setString(2, password);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                isAdminLoggedIn = true;
                viewAttendanceButton.setEnabled(true);
                eraseRecordsButton.setEnabled(true);
                JOptionPane.showMessageDialog(this, "Admin login successful.");
            } else {
                JOptionPane.showMessageDialog(this, "Invalid credentials.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error during login.", "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void viewAttendanceAction() {
        if (!isAdminLoggedIn) {
            JOptionPane.showMessageDialog(this, "Admin login required to view attendance.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            String query = "SELECT * FROM attendance ORDER BY date, employee_number";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            tableModel.setRowCount(0);
            while (rs.next()) {
                String empNumber = rs.getString("employee_number");
                String date = rs.getString("date");
                String timeIn = rs.getString("time_in");
                String timeOut = rs.getString("time_out");
                tableModel.addRow(new Object[]{empNumber, date, timeIn, timeOut});
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error retrieving attendance records.", "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void eraseRecordsAction(ActionEvent e) {
        if (!isAdminLoggedIn) {
            JOptionPane.showMessageDialog(this, "Admin login required to erase records.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to erase all attendance records?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                String query = "DELETE FROM attendance";
                Statement stmt = conn.createStatement();
                stmt.executeUpdate(query);
                tableModel.setRowCount(0);
                JOptionPane.showMessageDialog(this, "All attendance records have been erased.");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error erasing records.", "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AttendanceSystem().setVisible(true));
    }
}

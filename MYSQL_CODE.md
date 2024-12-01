CREATE DATABASE IF NOT EXISTS attendance_system;
USE attendance_system;
CREATE TABLE IF NOT EXISTS attendance (
    id INT AUTO_INCREMENT PRIMARY KEY,
    employee_number VARCHAR(10) NOT NULL,
    date DATE NOT NULL,
    time_in TIME DEFAULT NULL,
    time_out TIME DEFAULT NULL,
    UNIQUE (employee_number, date)
);
CREATE TABLE IF NOT EXISTS employees (
    employee_number VARCHAR(10) PRIMARY KEY,
    name VARCHAR(50) NOT NULL
);
INSERT INTO employees (employee_number, name) VALUES
('1001', 'Athien'),
('1002', 'Masi'),
('1003', 'Lawrence'),
('1004', 'Ryan'),
('1005', 'Joseph');
CREATE TABLE IF NOT EXISTS admins (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
);
INSERT INTO admins (username, password) VALUES
('admin', 'admin123');

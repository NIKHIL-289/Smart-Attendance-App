# Smart-Attendance-App
Smart Attendance Management System
A desktop application built with Advanced Java to manage student attendance, generate reports, and export data to CSV.

📌 Project Details
Field	Info
Project Title	Smart Attendance Management System
Course	Advanced Java Programming
Deadline	April 6, 2026
Language	Java 17+
GUI	Java Swing
Database	SQLite via JDBC
🚀 Features
Student Management — Add, update, delete, and search students with full CRUD operations.
Daily Attendance Marking — Select a course and date, then mark each student as Present / Absent / Late with a dropdown editor.
Batch Save — All attendance records for a session are saved in a single JDBC transaction with automatic rollback on failure.
Attendance Reports — View per-student attendance percentage against a configurable threshold (default 75 %).
Low Attendance Alerts — Students below the threshold are visually flagged in the report view.
CSV Export — One-click export of daily sheets or course summaries to a CSV file using Java NIO.
Persistent Storage — All data persists in a local SQLite database (attendance.db).
🏗️ Project Structure
SmartAttendanceManagement/
├── src/
│   └── main/java/com/attendance/
│       ├── Main.java                     ← Entry point
│       ├── model/
│       │   ├── Student.java
│       │   ├── Course.java
|       |   |── User.java
│       │   └── AttendanceRecord.java
│       ├── dao/
│       │   ├── GenericDAO.java           ← Generic DAO interface
│       │   ├── DatabaseConnection.java   ← Singleton JDBC connection
│       │   ├── StudentDAO.java
│       │   ├── CourseDAO.java
|       |   |── UserDAO.java
│       │   └── AttendanceDAO.java
│       ├── service/
│       │   └── AttendanceService.java    ← Stream API + business logic
│       ├── ui/
│       │   ├── MainFrame.java            ← Root Swing window
|       |   |── LoginFrame.java
│       │   ├── StudentPanel.java
│       │   ├── AttendancePanel.java
|       |   |── DashboardPanel.java
|       |   |── CoursePanel.java
│       │   └── ReportPanel.java
│       ├── util/
│       │   └── CSVExporter.java          ← Java NIO file export
│       └── exception/
│           └── AttendanceException.java
├── lib/
│   └── sqlite-jdbc-3.45.3.0.jar
└── README.md
⚙️ Setup & Run
Prerequisites
Java 17 or higher
Download sqlite-jdbc JAR and place it in /lib/
Compile
# Windows
javac -cp "lib/sqlite-jdbc-3.45.3.0.jar" -d out -sourcepath src/main/java src/main/java/com/attendance/Main.java
Run
# Windows
java -cp "out;lib/sqlite-jdbc-3.45.3.0.jar" com.attendance.Main
🔑 Advanced Java Concepts Used
Concept	Where Applied
JDBC	DatabaseConnection, StudentDAO, AttendanceDAO, CourseDAO
Singleton Pattern	DatabaseConnection.getInstance()
DAO Pattern	GenericDAO<T,ID> interface + all DAO classes
Generics	GenericDAO<T, ID>, Optional<T>, typed collections
Java Swing	All ui/ classes — JTable, JTabbedPane, JComboBox, etc.
Event Handling	ActionListener, ListSelectionListener, WindowAdapter
Stream API	AttendanceService — filter, map, collect, groupingBy
Lambda Expressions	Event listeners, stream operations throughout
JDBC Transactions	AttendanceDAO.insertBatch() — commit/rollback
SwingWorker	Background data loading in all three panels
Java NIO	CSVExporter — Files.newBufferedWriter, Paths.get
Custom Exceptions	AttendanceException hierarchy
Enum	AttendanceRecord.Status (PRESENT, ABSENT, LATE)
Serializable	Model classes implement Serializable
Optional	Null-safe DAO return values
Inner Classes	StatusColorRenderer inside AttendancePanel
Comparable	Student and AttendanceRecord for natural ordering
📸 Application Screens
Dashboard — Central hub for the application, providing real-time oversight of the institutional data.
Students Tab — Full CRUD form with a sortable student table.
Attendance Tab — Course + date picker loads all students; dropdown to mark status; color-coded rows (green = Present, red = Absent, yellow = Late).
Reports Tab — Per-student percentage table with configurable threshold and one-click CSV export.
Courses Tab — Structured management of the curriculum.
👥 Team Information
#	Name	Reg. No.	Responsibility
1	Dhriti Mittal	23BCE10704	Architecture, Database Design & GitHub Setup
2	Devashish Aswal	23BCE10273	Student & Course Management (DAO + Swing UI)
3	Ayan Tiwari	23BCE11580	Attendance Recording (DAO + Transactions + UI)
4	Nikhil Ranjan Tripathi	23BCE10262	Reports, Analytics (Stream API) & CSV Export
5	Vanshika Dhaka	23BCE11463	Integration, Testing, Exception Handling & Docs
📄 License
This project is submitted as an academic assignment for the Advanced Java Programming course.

Here is your **GitHub-ready README.md** recreated from your file, keeping the **same structure and content**, but formatted properly for GitHub:

---

# 📌 SMART ATTENDANCE MANAGEMENT SYSTEM

## 📖 Project Overview

The **Smart Attendance Management System** is a desktop application developed in Java that enables instructors to record, track, and report student attendance.

The application uses **Java Swing** for the graphical user interface and **SQLite via JDBC** for persistent data storage.

---

## ✨ Key Features

* **Student Management**: Full CRUD operations — add, edit, delete, and search students.
* **Daily Attendance Marking**: Select a course and date; mark each student as *Present, Absent, or Late* using a dropdown editor in a color-coded table.
* **Batch Save with Transactions**: All attendance records for a session are committed in a single JDBC transaction with automatic rollback on failure.
* **Attendance Reports**: Per-student attendance percentage with configurable threshold (default 75%). Students below the threshold are flagged visually.
* **CSV Export**: One-click export of course summaries to CSV using Java NIO (`java.nio.file.Files`).
* **Persistent Storage**: All data is stored in a local SQLite database (`attendance.db`) that persists between sessions.

---

## 📌 Project Details

| Field                   | Details                                              |
| ----------------------- | ---------------------------------------------------- |
| **Project Title**       | Smart Attendance Management System                   |
| **Course**              | Advanced Java Programming (4019)                     |
| **Submitted On**        | April 6, 2026 (Project) <br> April 8, 2026 (Reports) |
| **Repository Access**   | Public                                               |
| **Language / Platform** | Java 17+ with SQLite JDBC & Swing GUI                |

---

## 📂 Project Structure

```
SmartAttendanceManagement/
├── src/
│   └── main/java/com/attendance/
│       ├── Main.java                     ← Entry point
│       ├── model/
│       │   ├── Student.java
│       │   ├── Course.java
│       │   ├── User.java
│       │   └── AttendanceRecord.java
│       ├── dao/
│       │   ├── GenericDAO.java           ← Generic DAO interface
│       │   ├── DatabaseConnection.java   ← Singleton JDBC connection
│       │   ├── StudentDAO.java
│       │   ├── CourseDAO.java
│       │   ├── UserDAO.java
│       │   └── AttendanceDAO.java
│       ├── service/
│       │   └── AttendanceService.java    ← Stream API + business logic
│       ├── ui/
│       │   ├── MainFrame.java            ← Root Swing window
│       │   ├── LoginFrame.java
│       │   ├── StudentPanel.java
│       │   ├── AttendancePanel.java
│       │   ├── DashboardPanel.java
│       │   ├── CoursePanel.java
│       │   └── ReportPanel.java
│       ├── util/
│       │   └── CSVExporter.java          ← Java NIO file export
│       └── exception/
│           └── AttendanceException.java
├── lib/
│   └── sqlite-jdbc-3.45.3.0.jar
└── README.md
```

---

## ▶️ How to Run

### 🔧 Compile

```bash
javac -cp "lib/sqlite-jdbc-3.45.3.0.jar" -d out -sourcepath src/main/java \
src/main/java/com/attendance/Main.java
```

### ▶️ Run

```bash
java -cp "out;lib/sqlite-jdbc-3.45.3.0.jar" com.attendance.Main
```

---

## 📌 Notes

* Ensure **Java 17+** is installed
* Keep the **SQLite JDBC JAR** inside the `lib` folder
* The database file (`attendance.db`) will be created automatically

---

## 📄 End of Report

**Report 1 – README File**

---


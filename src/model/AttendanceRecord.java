package com.attendance.model;

import java.time.LocalDate;

/**
 * Represents a single attendance record for a student on a specific date.
 * Uses Java 8 LocalDate instead of legacy java.util.Date.
 */
public class AttendanceRecord {

    public enum Status { PRESENT, ABSENT, LATE }

    private int       id;
    private int       studentId;
    private int       courseId;
    private LocalDate date;
    private Status    status;

    // For display (from JOIN queries)
    private String studentName;
    private String rollNumber;
    private String courseName;

    public AttendanceRecord() {}

    public AttendanceRecord(int id, int studentId, int courseId, LocalDate date, Status status) {
        this.id        = id;
        this.studentId = studentId;
        this.courseId  = courseId;
        this.date      = date;
        this.status    = status;
    }

    public int       getId()                    { return id; }
    public void      setId(int id)              { this.id = id; }

    public int       getStudentId()             { return studentId; }
    public void      setStudentId(int s)        { this.studentId = s; }

    public int       getCourseId()              { return courseId; }
    public void      setCourseId(int c)         { this.courseId = c; }

    public LocalDate getDate()                  { return date; }
    public void      setDate(LocalDate d)       { this.date = d; }

    public Status    getStatus()                { return status; }
    public void      setStatus(Status s)        { this.status = s; }

    public String    getStudentName()           { return studentName; }
    public void      setStudentName(String sn)  { this.studentName = sn; }

    public String    getRollNumber()            { return rollNumber; }
    public void      setRollNumber(String r)    { this.rollNumber = r; }

    public String    getCourseName()            { return courseName; }
    public void      setCourseName(String cn)   { this.courseName = cn; }

    @Override
    public String toString() {
        return studentName + " | " + date + " | " + status;
    }
}

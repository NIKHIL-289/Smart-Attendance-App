package com.attendance.model;

/**
 * Represents a Student entity.
 * Demonstrates Java encapsulation and proper POJO design.
 */
public class Student {

    private int    id;
    private String rollNumber;
    private String name;
    private String email;
    private String phone;
    private int    courseId;
    private String courseName; // for display only (JOIN result)

    public Student() {}

    public Student(int id, String rollNumber, String name, String email, String phone, int courseId) {
        this.id         = id;
        this.rollNumber = rollNumber;
        this.name       = name;
        this.email      = email;
        this.phone      = phone;
        this.courseId   = courseId;
    }

    // Getters & Setters
    public int    getId()                   { return id; }
    public void   setId(int id)             { this.id = id; }

    public String getRollNumber()           { return rollNumber; }
    public void   setRollNumber(String r)   { this.rollNumber = r; }

    public String getName()                 { return name; }
    public void   setName(String n)         { this.name = n; }

    public String getEmail()                { return email; }
    public void   setEmail(String e)        { this.email = e; }

    public String getPhone()                { return phone; }
    public void   setPhone(String p)        { this.phone = p; }

    public int    getCourseId()             { return courseId; }
    public void   setCourseId(int c)        { this.courseId = c; }

    public String getCourseName()           { return courseName; }
    public void   setCourseName(String cn)  { this.courseName = cn; }

    @Override
    public String toString() {
        return rollNumber + " - " + name;
    }
}

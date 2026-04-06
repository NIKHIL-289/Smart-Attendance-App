package com.attendance.model;

/**
 * Represents a Course / Subject entity.
 */
public class Course {

    private int    id;
    private String code;
    private String name;
    private String instructor;
    private int    totalClasses;

    public Course() {}

    public Course(int id, String code, String name, String instructor, int totalClasses) {
        this.id           = id;
        this.code         = code;
        this.name         = name;
        this.instructor   = instructor;
        this.totalClasses = totalClasses;
    }

    public int    getId()                      { return id; }
    public void   setId(int id)                { this.id = id; }

    public String getCode()                    { return code; }
    public void   setCode(String c)            { this.code = c; }

    public String getName()                    { return name; }
    public void   setName(String n)            { this.name = n; }

    public String getInstructor()              { return instructor; }
    public void   setInstructor(String i)      { this.instructor = i; }

    public int    getTotalClasses()            { return totalClasses; }
    public void   setTotalClasses(int t)       { this.totalClasses = t; }

    @Override
    public String toString() {
        return code + " - " + name;
    }
}

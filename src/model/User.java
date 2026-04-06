package com.attendance.model;

/**
 * Represents an application user (Admin or Teacher).
 * Model class following MVC architecture.
 */
public class User {

    public enum Role { ADMIN, TEACHER }

    private int id;
    private String username;
    private String password;
    private String fullName;
    private Role role;

    public User() {}

    public User(int id, String username, String password, String fullName, Role role) {
        this.id       = id;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.role     = role;
    }

    // Getters & Setters
    public int    getId()       { return id; }
    public void   setId(int id) { this.id = id; }

    public String getUsername()              { return username; }
    public void   setUsername(String u)      { this.username = u; }

    public String getPassword()              { return password; }
    public void   setPassword(String p)      { this.password = p; }

    public String getFullName()              { return fullName; }
    public void   setFullName(String n)      { this.fullName = n; }

    public Role   getRole()                  { return role; }
    public void   setRole(Role r)            { this.role = r; }

    @Override
    public String toString() {
        return fullName + " (" + role + ")";
    }
}

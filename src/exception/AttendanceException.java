package com.attendance.exception;

/**
 * Custom checked exception for the Attendance Management System.
 * Demonstrates custom exception handling – an Advanced Java concept.
 */
public class AttendanceException extends Exception {

    private final int errorCode;

    public AttendanceException(String message) {
        super(message);
        this.errorCode = 0;
    }

    public AttendanceException(String message, int errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public AttendanceException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = 0;
    }

    public int getErrorCode() {
        return errorCode;
    }

    // Error code constants
    public static final int DB_ERROR         = 1001;
    public static final int NOT_FOUND        = 1002;
    public static final int DUPLICATE_ENTRY  = 1003;
    public static final int INVALID_DATA     = 1004;
}

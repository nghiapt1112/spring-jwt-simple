package com.example.demo.infrastructure.exception;

/**
 * Exception thrown for security-related issues
 */
public class SecurityException extends RuntimeException {
    
    private final String errorCode;
    
    /**
     * Constructs a new SecurityException
     * 
     * @param errorCode Error code for categorization
     * @param message Detailed error message
     */
    public SecurityException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    /**
     * Get the error code
     * 
     * @return Error code
     */
    public String getErrorCode() {
        return errorCode;
    }
}

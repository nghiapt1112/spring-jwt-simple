package com.digitalwallet.exception;

/**
 * Exception thrown when identity verification fails.
 */
public class IdentityVerificationException extends Exception {
    
    public IdentityVerificationException(String message) {
        super(message);
    }
    
    public IdentityVerificationException(String message, Throwable cause) {
        super(message, cause);
    }
}
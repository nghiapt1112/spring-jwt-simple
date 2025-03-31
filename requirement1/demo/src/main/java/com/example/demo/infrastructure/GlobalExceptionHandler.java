package com.example.demo.infrastructure;

import com.example.demo.infrastructure.exception.InvalidTransactionException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for the application
 * Handles security and validation exceptions
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle access denied exceptions
     * @param ex Access denied exception
     * @return Error response with 403 status
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDeniedException(AccessDeniedException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", "Access denied: You don't have permission to perform this operation");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }
    
    /**
     * Handle authentication exceptions
     * @param ex Authentication exception
     * @return Error response with 401 status
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleAuthenticationException(AuthenticationException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", "Authentication failed");
        
        if (ex instanceof BadCredentialsException) {
            response.put("message", "Invalid username or password");
        }
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }
    
    /**
     * Handle JWT exceptions
     */
    @ExceptionHandler({
        ExpiredJwtException.class,
        UnsupportedJwtException.class,
        MalformedJwtException.class,
        SignatureException.class,
        IllegalArgumentException.class
    })
    public ResponseEntity<Map<String, Object>> handleJwtException(Exception ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "error");
        
        if (ex instanceof ExpiredJwtException) {
            response.put("message", "Token has expired");
        } else if (ex instanceof UnsupportedJwtException) {
            response.put("message", "Unsupported token");
        } else if (ex instanceof MalformedJwtException || ex instanceof SignatureException) {
            response.put("message", "Invalid token");
        } else {
            response.put("message", "Token validation error");
        }
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }
    
    /**
     * Handle invalid transaction exceptions
     * @param ex Invalid transaction exception
     * @return Error response with 400 status
     */
    @ExceptionHandler(InvalidTransactionException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidTransactionException(InvalidTransactionException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    /**
     * Handle 404 Not Found
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNoHandlerFoundException(NoHandlerFoundException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", "Resource not found");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
    
    /**
     * Fallback exception handler
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", "An unexpected error occurred");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}

package com.digitalwallet.service;

import com.digitalwallet.model.Transaction;
import com.digitalwallet.model.User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for transaction operations.
 * This is a stub implementation for demonstration purposes.
 */
@Service
public class TransactionService {
    
    // In-memory user storage for demo purposes
    private final Map<String, User> userMap = new HashMap<>();
    
    public TransactionService() {
        // Add a sample user for testing
        User user = new User();
        user.setId("user123");
        user.setFullName("Jane Doe");
        user.setEmail("jane.doe@example.com");
        user.setDocumentNumber("A12345678");
        user.setDocumentType("PASSPORT");
        user.setDateOfBirth(LocalDateTime.of(1990, 1, 1, 0, 0));
        user.setMfaEnabled(true);
        user.setVerified(true);
        
        userMap.put(user.getId(), user);
    }
    
    /**
     * Get a user by their ID.
     * 
     * @param userId The ID of the user to retrieve
     * @return The user with the specified ID
     */
    public User getUserById(String userId) {
        return userMap.get(userId);
    }
    
    /**
     * Process a transaction.
     * 
     * @param transaction The transaction to process
     * @return The processed transaction
     */
    public Transaction processTransaction(Transaction transaction) {
        // In a real application, this would process the transaction
        // For this example, we'll just mark it as verified
        transaction.setVerified(true);
        
        return transaction;
    }
}
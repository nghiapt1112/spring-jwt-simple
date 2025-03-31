package com.digitalwallet.controller;

import com.digitalwallet.exception.IdentityVerificationException;
import com.digitalwallet.model.Transaction;
import com.digitalwallet.model.User;
import com.digitalwallet.model.VerificationResult;
import com.digitalwallet.service.TransactionService;
import com.digitalwallet.service.identity.IdentityVerificationService;
import com.digitalwallet.service.identity.VerificationLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

/**
 * Controller for transaction operations.
 * Demonstrates the usage of identity verification service.
 */
@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {
    
    private static final Logger logger = LoggerFactory.getLogger(TransactionController.class);
    
    private final TransactionService transactionService;
    private final IdentityVerificationService identityVerificationService;
    
    @Autowired
    public TransactionController(
            TransactionService transactionService,
            IdentityVerificationService identityVerificationService) {
        this.transactionService = transactionService;
        this.identityVerificationService = identityVerificationService;
    }
    
    /**
     * Endpoint for creating a new transaction.
     * Demonstrates identity verification before allowing transaction.
     */
    @PostMapping
    public ResponseEntity<?> createTransaction(@RequestBody Transaction transaction) {
        try {
            // Get user from service
            User user = transactionService.getUserById(transaction.getUserId());
            
            // Determine verification level based on transaction amount
            VerificationLevel verificationLevel = determineVerificationLevel(transaction.getAmount());
            
            // Verify user identity
            VerificationResult verificationResult = identityVerificationService.verifyIdentityForTransaction(
                    user, transaction, verificationLevel);
            
            // Check if verification was successful
            if (!verificationResult.isVerified()) {
                logger.warn("Identity verification failed for transaction: {}", transaction.getId());
                return ResponseEntity.badRequest().body(verificationResult);
            }
            
            // Process the transaction
            Transaction processedTransaction = transactionService.processTransaction(transaction);
            
            return ResponseEntity.ok(processedTransaction);
        } catch (IdentityVerificationException e) {
            logger.error("Error during identity verification", e);
            return ResponseEntity.badRequest().body("Identity verification failed: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error processing transaction", e);
            return ResponseEntity.internalServerError().body("Error processing transaction: " + e.getMessage());
        }
    }
    
    /**
     * Determine the verification level based on transaction amount.
     */
    private VerificationLevel determineVerificationLevel(BigDecimal amount) {
        if (amount.compareTo(new BigDecimal("10000")) >= 0) {
            return VerificationLevel.HIGH;
        } else if (amount.compareTo(new BigDecimal("1000")) >= 0) {
            return VerificationLevel.MEDIUM;
        } else {
            return VerificationLevel.LOW;
        }
    }
}
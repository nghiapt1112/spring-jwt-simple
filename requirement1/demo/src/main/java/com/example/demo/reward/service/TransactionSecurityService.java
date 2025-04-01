package com.example.demo.reward.service;

import com.example.demo.dto.IdentityVerificationRequest;
import com.example.demo.dto.IdentityVerificationResult;
import com.example.demo.infrastructure.exception.SecurityException;
import com.example.demo.service.identity.IdentityVerificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service that provides transaction security by integrating identity verification
 * with the wallet operations. This service implements a risk-based approach 
 * where different transactions may require different levels of verification.
 */
@Service
public class TransactionSecurityService {
    private static final Logger log = LoggerFactory.getLogger(TransactionSecurityService.class);
    
    // Transaction amount thresholds for risk assessment
    private static final double MEDIUM_RISK_THRESHOLD = 1000.0;
    private static final double HIGH_RISK_THRESHOLD = 5000.0;
    
    // Cache of verified sessions to prevent repeated verifications in a short time
    private final Map<String, VerificationSession> verificationSessions = new ConcurrentHashMap<>();
    
    private final IdentityVerificationService identityVerificationService;
    
    @Autowired
    public TransactionSecurityService(IdentityVerificationService identityVerificationService) {
        this.identityVerificationService = identityVerificationService;
    }
    
    /**
     * Verify user identity before a transaction based on risk assessment
     * 
     * @param userId User ID
     * @param transactionAmount Transaction amount
     * @param transactionType Type of transaction (e.g., EARN_POINTS, REDEEM_POINTS)
     * @param deviceInfo Additional device information for security checks
     * @return true if verification successful, throws SecurityException otherwise
     */
    public boolean verifyTransactionSecurity(
            String userId, 
            double transactionAmount, 
            String transactionType,
            Map<String, String> deviceInfo) {
        
        log.info("Security verification for userId: {}, transaction: {} {}", 
                userId, transactionAmount, transactionType);
        
        // Check if user already has a valid verification session
        VerificationSession session = verificationSessions.get(userId);
        if (isSessionValid(session, transactionAmount)) {
            log.info("Using existing verification session for user: {}", userId);
            return true;
        }
        
        // Determine risk level and verification requirements
        VerificationLevel requiredLevel = determineRequiredVerificationLevel(
                transactionAmount, transactionType);
        
        log.info("Required verification level for transaction: {}", requiredLevel);
        
        // Build verification request based on risk level
        IdentityVerificationRequest.IdentityVerificationRequestBuilder requestBuilder = IdentityVerificationRequest.builder()
                .userId(userId)
                .transactionAmount(transactionAmount)
                .transactionType(transactionType);
        
        // Add device information if available
        if (deviceInfo != null) {
            requestBuilder
                    .deviceId(deviceInfo.get("deviceId"))
                    .ipAddress(deviceInfo.get("ipAddress"))
                    .geoLocation(deviceInfo.get("geoLocation"));
        }
        
        // Set MFA requirement based on risk level
        boolean requireMfa = requiredLevel == VerificationLevel.HIGH;
        requestBuilder.requireMfa(requireMfa);
        
        // Set high risk mode for sensitive operations
        requestBuilder.highRiskMode(requiredLevel == VerificationLevel.HIGH);
        
        // Execute verification
        IdentityVerificationResult result = identityVerificationService.verifyIdentity(
                requestBuilder.build());
        
        // Handle MFA requirements
        if (result.isMfaRequired()) {
            throw new SecurityException("MFA_REQUIRED", 
                    "Multi-factor authentication required for this transaction");
        }
        
        // Handle verification failures
        if (!result.isVerified()) {
            throw new SecurityException(
                    result.getErrorCode(),
                    "Identity verification failed: " + result.getErrorMessage());
        }
        
        // Store valid session for future transactions
        verificationSessions.put(userId, new VerificationSession(
                result.getVerificationId(),
                result.getVerifiedAt(),
                result.getExpiresAt(),
                getVerificationLevelFromResult(result),
                transactionAmount
        ));
        
        return true;
    }
    
    /**
     * Verify a multi-factor authentication code
     * 
     * @param userId User ID
     * @param verificationCode Code to verify
     * @return true if verification successful, throws SecurityException otherwise
     */
    public boolean verifyMfaCode(String userId, String verificationCode) {
        IdentityVerificationRequest request = IdentityVerificationRequest.builder()
                .userId(userId)
                .verificationCode(verificationCode)
                .build();
        
        IdentityVerificationResult result = identityVerificationService.verifyIdentity(request);
        
        if (!result.isVerified()) {
            throw new SecurityException(
                    result.getErrorCode(),
                    "MFA verification failed: " + result.getErrorMessage());
        }
        
        // Update verification session with MFA verification
        VerificationSession existingSession = verificationSessions.get(userId);
        if (existingSession != null) {
            verificationSessions.put(userId, new VerificationSession(
                    result.getVerificationId(),
                    result.getVerifiedAt(),
                    result.getExpiresAt(),
                    VerificationLevel.HIGH, // MFA verified sessions are always high level
                    existingSession.getLastTransactionAmount()
            ));
        }
        
        return true;
    }
    
    /**
     * Check if an existing verification session is valid for the current transaction
     * 
     * @param session The verification session to check
     * @param transactionAmount Current transaction amount
     * @return true if session is valid, false otherwise
     */
    private boolean isSessionValid(VerificationSession session, double transactionAmount) {
        if (session == null) {
            return false;
        }
        
        // Check if session has expired
        if (session.hasExpired()) {
            return false;
        }
        
        // Check if transaction amount requires higher verification level
        VerificationLevel requiredLevel = determineRequiredVerificationLevel(
                transactionAmount, null);
        
        // Ensure session has sufficient verification level
        return session.getLevel().isAtLeast(requiredLevel);
    }
    
    /**
     * Determine the required verification level based on transaction details
     * 
     * @param transactionAmount Transaction amount
     * @param transactionType Transaction type
     * @return Required verification level
     */
    private VerificationLevel determineRequiredVerificationLevel(
            double transactionAmount, String transactionType) {
        
        // Higher amounts require stronger verification
        if (transactionAmount >= HIGH_RISK_THRESHOLD) {
            return VerificationLevel.HIGH;
        } else if (transactionAmount >= MEDIUM_RISK_THRESHOLD) {
            return VerificationLevel.MEDIUM;
        }
        
        // Certain transaction types might always require higher verification
        if ("REDEEM_POINTS".equals(transactionType)) {
            return VerificationLevel.MEDIUM;
        }
        
        return VerificationLevel.LOW;
    }
    
    /**
     * Map verification result to verification level
     * 
     * @param result Verification result
     * @return Verification level
     */
    private VerificationLevel getVerificationLevelFromResult(IdentityVerificationResult result) {
        if (result.getVerificationLevel() == null) {
            return VerificationLevel.LOW;
        }
        
        switch (result.getVerificationLevel()) {
            case "MFA_VERIFIED":
                return VerificationLevel.HIGH;
            case "ENHANCED":
                return VerificationLevel.MEDIUM;
            case "STANDARD":
            default:
                return VerificationLevel.LOW;
        }
    }
    
    /**
     * Verification session to track successful verification
     */
    private static class VerificationSession {
        private final String verificationId;
        private final Instant verifiedAt;
        private final Instant expiresAt;
        private final VerificationLevel level;
        private final double lastTransactionAmount;
        
        public VerificationSession(
                String verificationId,
                Instant verifiedAt,
                Instant expiresAt,
                VerificationLevel level,
                double lastTransactionAmount) {
            this.verificationId = verificationId;
            this.verifiedAt = verifiedAt;
            this.expiresAt = expiresAt;
            this.level = level;
            this.lastTransactionAmount = lastTransactionAmount;
        }
        
        public boolean hasExpired() {
            return Instant.now().isAfter(expiresAt);
        }
        
        public VerificationLevel getLevel() {
            return level;
        }
        
        public double getLastTransactionAmount() {
            return lastTransactionAmount;
        }
    }
    
    /**
     * Verification levels with increasing security requirements
     */
    public enum VerificationLevel {
        LOW, MEDIUM, HIGH;
        
        public boolean isAtLeast(VerificationLevel other) {
            return this.ordinal() >= other.ordinal();
        }
    }
}

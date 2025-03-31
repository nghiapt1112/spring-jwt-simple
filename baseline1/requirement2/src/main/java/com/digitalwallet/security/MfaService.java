package com.digitalwallet.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Service responsible for multi-factor authentication.
 */
@Service
public class MfaService {

    private static final Logger logger = LoggerFactory.getLogger(MfaService.class);
    
    // In a real application, this would be backed by a database or cache
    private final Map<String, MfaStatus> mfaStatusMap = new HashMap<>();
    
    /**
     * Verify if MFA has been completed for a user.
     * 
     * @param userId The user ID to check
     * @return true if MFA has been completed and is still valid, false otherwise
     */
    public boolean verifyMfaCompletion(String userId) {
        MfaStatus status = mfaStatusMap.get(userId);
        if (status == null) {
            logger.info("No MFA status found for user: {}", userId);
            return false;
        }
        
        // Check if MFA is still valid (completed within the last 15 minutes)
        boolean isValid = status.isCompleted() && 
                          status.getCompletionTime().plusMinutes(15).isAfter(LocalDateTime.now());
        
        if (!isValid) {
            logger.info("MFA expired for user: {}", userId);
            // Remove expired MFA status
            mfaStatusMap.remove(userId);
        }
        
        return isValid;
    }
    
    /**
     * Initiate MFA for a user.
     * 
     * @param userId The user ID to initiate MFA for
     * @return A challenge ID for the MFA process
     */
    public String initiateMfa(String userId) {
        String challengeId = generateChallengeId();
        
        MfaStatus status = new MfaStatus();
        status.setUserId(userId);
        status.setChallengeId(challengeId);
        status.setInitiationTime(LocalDateTime.now());
        status.setCompleted(false);
        
        mfaStatusMap.put(userId, status);
        logger.info("Initiated MFA for user: {}", userId);
        
        return challengeId;
    }
    
    /**
     * Complete MFA for a user.
     * 
     * @param userId The user ID
     * @param challengeId The challenge ID
     * @param code The verification code
     * @return true if MFA was completed successfully, false otherwise
     */
    public boolean completeMfa(String userId, String challengeId, String code) {
        MfaStatus status = mfaStatusMap.get(userId);
        if (status == null || !status.getChallengeId().equals(challengeId)) {
            logger.warn("Invalid MFA challenge for user: {}", userId);
            return false;
        }
        
        // In a real implementation, we would verify the code against the expected value
        // For this example, we'll simulate a successful verification
        
        status.setCompleted(true);
        status.setCompletionTime(LocalDateTime.now());
        mfaStatusMap.put(userId, status);
        
        logger.info("Completed MFA for user: {}", userId);
        return true;
    }
    
    /**
     * Generate a random challenge ID.
     */
    private String generateChallengeId() {
        return UUID.randomUUID().toString();
    }
    
    /**
     * Internal class for tracking MFA status.
     */
    private static class MfaStatus {
        private String userId;
        private String challengeId;
        private LocalDateTime initiationTime;
        private LocalDateTime completionTime;
        private boolean completed;
        
        public String getUserId() {
            return userId;
        }
        
        public void setUserId(String userId) {
            this.userId = userId;
        }
        
        public String getChallengeId() {
            return challengeId;
        }
        
        public void setChallengeId(String challengeId) {
            this.challengeId = challengeId;
        }
        
        public LocalDateTime getInitiationTime() {
            return initiationTime;
        }
        
        public void setInitiationTime(LocalDateTime initiationTime) {
            this.initiationTime = initiationTime;
        }
        
        public LocalDateTime getCompletionTime() {
            return completionTime;
        }
        
        public void setCompletionTime(LocalDateTime completionTime) {
            this.completionTime = completionTime;
        }
        
        public boolean isCompleted() {
            return completed;
        }
        
        public void setCompleted(boolean completed) {
            this.completed = completed;
        }
    }
}
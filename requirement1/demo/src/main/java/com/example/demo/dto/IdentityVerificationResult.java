package com.example.demo.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

/**
 * Result object for identity verification
 */
@Data
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class IdentityVerificationResult {
    
    // Status of the verification
    private boolean verified;
    private boolean mfaRequired;
    
    // Reference IDs
    private String verificationId;    // ID from the verification provider
    private String verificationLevel; // Level of verification (STANDARD, ENHANCED, MFA_VERIFIED)
    
    // Timestamps
    private Instant verifiedAt;
    private Instant expiresAt;
    
    // Error details (if verification failed)
    private String errorCode;
    private String errorMessage;
    
    /**
     * Factory method for successful verification
     * 
     * @param verificationId ID from the verification provider
     * @param verificationLevel Level of verification achieved
     * @return IdentityVerificationResult for success case
     */
    public static IdentityVerificationResult success(String verificationId, String verificationLevel) {
        Instant now = Instant.now();
        return IdentityVerificationResult.builder()
                .verified(true)
                .mfaRequired(false)
                .verificationId(verificationId)
                .verificationLevel(verificationLevel)
                .verifiedAt(now)
                .expiresAt(now.plusSeconds(3600)) // Valid for 1 hour
                .build();
    }
    
    /**
     * Factory method for MFA required response
     * 
     * @param message Information message about MFA
     * @return IdentityVerificationResult indicating MFA is required
     */
    public static IdentityVerificationResult mfaRequired(String message) {
        return IdentityVerificationResult.builder()
                .verified(false)
                .mfaRequired(true)
                .errorMessage(message)
                .build();
    }
    
    /**
     * Factory method for verification failure
     * 
     * @param errorMessage Error message
     * @param errorCode Error code
     * @return IdentityVerificationResult for failure case
     */
    public static IdentityVerificationResult failure(String errorMessage, String errorCode) {
        return IdentityVerificationResult.builder()
                .verified(false)
                .mfaRequired(false)
                .errorCode(errorCode)
                .errorMessage(errorMessage)
                .build();
    }
}

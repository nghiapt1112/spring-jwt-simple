package com.digitalwallet.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Model representing the result of an identity verification operation.
 */
@Data
@Builder
public class VerificationResult {
    private boolean verified;
    private String requestId;
    private String providerId;
    private LocalDateTime timestamp;
    private String message;
    private String verificationId;  // ID from third-party service
    private Integer confidenceScore; // Optional confidence score (0-100)
    private String rejectionReason;  // Reason for rejection, if not verified
}
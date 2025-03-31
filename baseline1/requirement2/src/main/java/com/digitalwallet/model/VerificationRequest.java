package com.digitalwallet.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Model representing a verification request to a third-party identity verification service.
 */
@Data
@Builder
public class VerificationRequest {
    private String requestId;
    private String userId;
    private String transactionId;
    private BigDecimal transactionAmount;
    private String encryptedUserData;
    private LocalDateTime timestamp;
    private String ipAddress;
    private String deviceId;
}
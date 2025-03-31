package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request object for identity verification
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IdentityVerificationRequest {
    
    @NotBlank(message = "User ID is required")
    private String userId;
    
    // Optional verification factors
    private String documentId;      // ID document number (e.g., passport, driver's license)
    private String documentType;    // Type of document (PASSPORT, DRIVERS_LICENSE, ID_CARD)
    private String verificationCode; // For MFA verification
    
    // Transaction details (for risk-based verification)
    private Double transactionAmount;
    private String transactionType;
    
    // Device and location information for risk analysis
    private String deviceId;
    private String ipAddress;
    private String geoLocation;
    
    // Control flags
    private boolean requireMfa;     // Whether MFA should be required
    private boolean highRiskMode;   // Apply additional verification for high-risk operations
}

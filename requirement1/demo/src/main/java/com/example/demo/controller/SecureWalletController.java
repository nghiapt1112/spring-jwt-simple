package com.example.demo.controller;

import com.example.demo.dto.EarnPointsRequest;
import com.example.demo.dto.IdentityVerificationRequest;
import com.example.demo.dto.IdentityVerificationResult;
import com.example.demo.infrastructure.exception.SecurityException;
import com.example.demo.model.PointTransaction;
import com.example.demo.service.SecureRewardService;
import com.example.demo.service.identity.IdentityVerificationService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller demonstrating secure wallet transactions with identity verification
 */
@RestController
@RequestMapping("/api/wallet")
public class SecureWalletController {
    private static final Logger log = LoggerFactory.getLogger(SecureWalletController.class);
    
    private final SecureRewardService secureRewardService;
    private final IdentityVerificationService identityVerificationService;
    
    @Autowired
    public SecureWalletController(
            SecureRewardService secureRewardService,
            IdentityVerificationService identityVerificationService) {
        this.secureRewardService = secureRewardService;
        this.identityVerificationService = identityVerificationService;
    }
    
    /**
     * Earns points for a user with identity verification
     * 
     * @param request The points earning request
     * @param deviceId Optional device ID
     * @param ipAddress Optional IP address
     * @return Response with earned points or error
     */
    @PostMapping("/earn-points")
    public ResponseEntity<?> earnPoints(
            @Valid @RequestBody EarnPointsRequest request,
            @RequestHeader(value = "X-Device-ID", required = false) String deviceId,
            @RequestHeader(value = "X-Forwarded-For", required = false) String ipAddress) {
        
        log.info("Earn points request for user: {}, amount: {}", 
                request.getUserId(), request.getTransactionAmount());
        
        try {
            // Collect device info for security verification
            Map<String, String> deviceInfo = new HashMap<>();
            if (deviceId != null) deviceInfo.put("deviceId", deviceId);
            if (ipAddress != null) deviceInfo.put("ipAddress", ipAddress);
            
            // Call secure service that performs verification before transaction
            int pointsEarned = secureRewardService.earnPoints(
                    request.getUserId(),
                    request.getTransactionAmount(),
                    deviceInfo);
            
            // Return success response
            Map<String, Object> response = new HashMap<>();
            response.put("userId", request.getUserId());
            response.put("pointsEarned", pointsEarned);
            response.put("newBalance", secureRewardService.getBalance(request.getUserId()));
            response.put("status", "SUCCESS");
            
            return ResponseEntity.ok(response);
        } catch (SecurityException e) {
            log.warn("Security error during earn points: {}", e.getMessage());
            
            // Special handling for MFA requirement
            if ("MFA_REQUIRED".equals(e.getErrorCode())) {
                return ResponseEntity.status(HttpStatus.ACCEPTED)
                        .body(Map.of(
                                "status", "MFA_REQUIRED",
                                "message", "Please complete identity verification",
                                "userId", request.getUserId()));
            }
            
            // Other security errors
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of(
                            "status", "ERROR",
                            "errorCode", e.getErrorCode(),
                            "message", e.getMessage()));
        } catch (Exception e) {
            log.error("Error processing earn points", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "status", "ERROR",
                            "message", "An unexpected error occurred"));
        }
    }
    
    /**
     * Verifies user identity directly (for initial verification or MFA)
     * 
     * @param request Identity verification request
     * @return Verification result
     */
    @PostMapping("/verify-identity")
    public ResponseEntity<?> verifyIdentity(@Valid @RequestBody IdentityVerificationRequest request) {
        log.info("Identity verification request for user: {}", request.getUserId());
        
        try {
            IdentityVerificationResult result = identityVerificationService.verifyIdentity(request);
            
            if (result.isMfaRequired()) {
                return ResponseEntity.status(HttpStatus.ACCEPTED)
                        .body(Map.of(
                                "status", "MFA_REQUIRED",
                                "message", result.getErrorMessage(),
                                "userId", request.getUserId()));
            }
            
            if (!result.isVerified()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of(
                                "status", "ERROR",
                                "errorCode", result.getErrorCode(),
                                "message", result.getErrorMessage()));
            }
            
            return ResponseEntity.ok(Map.of(
                    "status", "VERIFIED",
                    "userId", request.getUserId(),
                    "verificationId", result.getVerificationId(),
                    "verificationLevel", result.getVerificationLevel(),
                    "expiresAt", result.getExpiresAt().toString()));
        } catch (Exception e) {
            log.error("Error during identity verification", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "status", "ERROR",
                            "message", "An unexpected error occurred"));
        }
    }
    
    /**
     * Verifies an MFA code for a user
     * 
     * @param userId User ID
     * @param code Verification code
     * @return Verification result
     */
    @PostMapping("/verify-mfa")
    public ResponseEntity<?> verifyMfaCode(
            @RequestParam String userId,
            @RequestParam String code) {
        
        log.info("MFA verification for user: {}", userId);
        
        try {
            boolean verified = secureRewardService.verifyMfaCode(userId, code);
            
            return ResponseEntity.ok(Map.of(
                    "status", "VERIFIED",
                    "userId", userId,
                    "message", "MFA verification successful"));
        } catch (SecurityException e) {
            log.warn("MFA verification failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of(
                            "status", "ERROR",
                            "errorCode", e.getErrorCode(),
                            "message", e.getMessage()));
        } catch (Exception e) {
            log.error("Error during MFA verification", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "status", "ERROR",
                            "message", "An unexpected error occurred"));
        }
    }
    
    /**
     * Gets transaction history for a user
     * 
     * @param userId User ID
     * @return Transaction history
     */
    @GetMapping("/transactions/{userId}")
    public ResponseEntity<?> getTransactionHistory(@PathVariable String userId) {
        try {
            List<PointTransaction> transactions = secureRewardService.getTransactionHistory(userId);
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            log.error("Error retrieving transaction history", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "status", "ERROR",
                            "message", "An unexpected error occurred"));
        }
    }
    
    /**
     * Gets current balance for a user
     * 
     * @param userId User ID
     * @return Current balance
     */
    @GetMapping("/balance/{userId}")
    public ResponseEntity<?> getBalance(@PathVariable String userId) {
        try {
            int balance = secureRewardService.getBalance(userId);
            return ResponseEntity.ok(Map.of(
                    "userId", userId,
                    "balance", balance));
        } catch (Exception e) {
            log.error("Error retrieving balance", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "status", "ERROR",
                            "message", "An unexpected error occurred"));
        }
    }
}

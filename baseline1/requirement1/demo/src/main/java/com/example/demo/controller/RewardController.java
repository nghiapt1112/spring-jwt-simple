package com.example.demo.controller;

import com.example.demo.config.InsufficientPointsException;
import com.example.demo.service.RewardService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/rewards")
@Validated
public class RewardController {

    @Autowired
    private RewardService rewardService;

    /**
     * Earn reward points from a transaction
     * 
     * @param userId User ID
     * @param transactionAmount Transaction amount (must be positive)
     * @return Response with earned points information
     */
    @PostMapping("/earn")
    public ResponseEntity<Map<String, Object>> earnPoints(
            @RequestParam @NotBlank String userId,
            @RequestParam @Positive double transactionAmount) {
        
        // Authenticate user
        validateUserAccess(userId);
        
        // Process points earning
        int pointsEarned = rewardService.earnPoints(userId, transactionAmount);
        int newBalance = rewardService.getBalance(userId);

        // Build response
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Points earned successfully");
        response.put("pointsEarned", pointsEarned);
        response.put("newBalance", newBalance);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Redeem reward points
     * 
     * @param userId User ID
     * @param points Points to redeem (must be positive)
     * @return Response with redemption status
     */
    @PostMapping("/redeem")
    public ResponseEntity<Map<String, Object>> redeemPoints(
            @RequestParam @NotBlank String userId,
            @RequestParam @Min(1) int points) {
        
        // Authenticate user
        validateUserAccess(userId);
        
        // Process redemption
        rewardService.redeemPoints(userId, points);
        int newBalance = rewardService.getBalance(userId);

        // Build response
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Points redeemed successfully");
        response.put("pointsRedeemed", points);
        response.put("newBalance", newBalance);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Check reward points balance
     * 
     * @param userId User ID
     * @return Response with balance information
     */
    @GetMapping("/balance")
    public ResponseEntity<Map<String, Object>> getBalance(
            @RequestParam @NotBlank String userId) {
        
        // Authenticate user
        validateUserAccess(userId);
        
        // Get current balance
        int balance = rewardService.getBalance(userId);

        // Build response
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("userId", userId);
        response.put("balance", balance);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Validate that the authenticated user has access to the requested user ID
     * 
     * @param userId User ID to validate access for
     * @throws SecurityException if access is not allowed
     */
    private void validateUserAccess(String userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        // In a real application, you would check if the authenticated user has permission
        // to access the requested userId (e.g., same user, admin role, etc.)
        // For simplicity in this demo, we just check if the user is authenticated
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("User not authenticated");
        }
        
        // Additional access controls can be added here
    }

    /**
     * Handle case where user does not exist
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleUserNotFound(IllegalArgumentException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
    
    /**
     * Handle security exceptions
     */
    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<Map<String, Object>> handleSecurityException(SecurityException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", "Access denied");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }
}

package com.example.demo.controller;

import com.example.demo.model.PointTransaction;
import com.example.demo.service.RewardService;
import com.example.demo.dto.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/rewards")
@Validated
public class RewardController {

    @Autowired
    private RewardService rewardService;

    /**
     * Earn reward points from a transaction
     * 
     * @param request Request containing user ID and transaction amount
     * @return Earned points information
     */
    @PostMapping("/earn")
    public ResponseEntity<EarnPointsResponse> earnPoints(
            @Valid @RequestBody EarnPointsRequest request) {
        
        // Process points earning
        int pointsEarned = rewardService.earnPoints(request.getUserId(), request.getTransactionAmount());
        int newBalance = rewardService.getBalance(request.getUserId());

        return ResponseEntity.ok(new EarnPointsResponse(pointsEarned, newBalance));
    }

    /**
     * Redeem reward points
     * 
     * @param request Request containing user ID and points to redeem
     * @return Redemption status
     */
    @PostMapping("/redeem")
    public ResponseEntity<RedeemPointsResponse> redeemPoints(
            @Valid @RequestBody RedeemPointsRequest request) {
        
        // Process redemption
        rewardService.redeemPoints(request.getUserId(), request.getPoints());
        int newBalance = rewardService.getBalance(request.getUserId());

        return ResponseEntity.ok(new RedeemPointsResponse(request.getPoints(), newBalance));
    }

    /**
     * Check reward points balance
     * 
     * @param userId User ID
     * @return Balance information
     */
    @GetMapping("/balance")
    public ResponseEntity<BalanceResponse> getBalance(
            @RequestParam @NotBlank String userId) {
        
        // Get current balance
        int balance = rewardService.getBalance(userId);

        return ResponseEntity.ok(new BalanceResponse(userId, balance));
    }
    
    /**
     * Get transaction history
     * 
     * @param userId User ID
     * @return Transaction history
     */
    @GetMapping("/transactions")
    public ResponseEntity<TransactionsResponse> getTransactionHistory(
            @RequestParam @NotBlank String userId) {
        
        // Get transaction history
        List<PointTransaction> transactions = rewardService.getTransactionHistory(userId);
        
        // Transform transactions
        List<TransactionDto> transactionsList = transactions.stream()
                .map(transaction -> new TransactionDto(
                    transaction.getType().name(),
                    transaction.getPoints(),
                    transaction.getDescription(),
                    transaction.getTimestamp().toString()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(new TransactionsResponse(userId, transactionsList));
    }

    /**
     * Handle case where user does not exist
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleUserNotFound(IllegalArgumentException ex) {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(Map.of("message", ex.getMessage()));
    }
    
    /**
     * Handle security exceptions
     */
    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<Map<String, String>> handleSecurityException(SecurityException ex) {
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(Map.of("message", "Access denied"));
    }
}

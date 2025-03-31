package com.example.demo.service;

import com.example.demo.config.InsufficientPointsException;
import com.example.demo.config.InvalidTransactionException;
import com.example.demo.model.UserRewardPoints;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RewardService {

    private final Map<String, UserRewardPoints> userPointsMap = new ConcurrentHashMap<>();

    public RewardService() {
        // Initialize with a user for demonstration purposes
        userPointsMap.put("user123", new UserRewardPoints("user123"));
    }

    /**
     * Earn points from a transaction
     * @param userId the user ID
     * @param transactionAmount the transaction amount
     * @return the number of points earned
     * @throws InvalidTransactionException if transaction amount is invalid
     */
    public int earnPoints(String userId, double transactionAmount) {
        if (transactionAmount <= 0) {
            throw new InvalidTransactionException("Transaction amount must be positive");
        }

        // Create user if not exists
        ensureUserExists(userId);
        
        int pointsEarned = calculatePointsEarned(transactionAmount);
        userPointsMap.get(userId).addPoints(pointsEarned);
        return pointsEarned;
    }

    /**
     * Redeem points
     * @param userId the user ID
     * @param pointsToRedeem the points to redeem
     * @return true if redeemed successfully
     * @throws InsufficientPointsException if user doesn't have enough points
     */
    public boolean redeemPoints(String userId, int pointsToRedeem) {
        if (pointsToRedeem <= 0) {
            throw new InvalidTransactionException("Points to redeem must be positive");
        }

        ensureUserExists(userId);
        
        UserRewardPoints userRewardPoints = userPointsMap.get(userId);
        if (userRewardPoints.getRewardPoints() < pointsToRedeem) {
            throw new InsufficientPointsException("Insufficient points to redeem: " + 
                userRewardPoints.getRewardPoints() + " available, " + pointsToRedeem + " requested");
        }
        
        return userRewardPoints.deductPoints(pointsToRedeem);
    }

    /**
     * Get the current balance of points for a user
     * @param userId the user ID
     * @return the point balance
     */
    public int getBalance(String userId) {
        ensureUserExists(userId);
        return userPointsMap.get(userId).getRewardPoints();
    }

    /**
     * Check if a user exists
     * @param userId the user ID
     * @return true if the user exists
     */
    public boolean userExists(String userId) {
        return userPointsMap.containsKey(userId);
    }
    
    /**
     * Ensure a user exists, create if not
     * @param userId the user ID
     */
    private void ensureUserExists(String userId) {
        userPointsMap.computeIfAbsent(userId, UserRewardPoints::new);
    }
    
    /**
     * Calculate points earned from a transaction
     * @param transactionAmount the transaction amount
     * @return the points earned
     */
    private int calculatePointsEarned(double transactionAmount) {
        // Formula: 10 points per unit of currency
        return (int) (transactionAmount * 10);
    }
}

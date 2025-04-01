package com.loyalty.wallet.service;

import com.loyalty.wallet.model.UserRewards;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RewardsService {
    // In-memory storage using ConcurrentHashMap for thread safety
    private final Map<String, UserRewards> userRewardsMap = new ConcurrentHashMap<>();

    // Points calculation: 1 point per $1 spent
    private static final int POINTS_PER_DOLLAR = 1;

    // Initialize user with 500 points if not already present
    public UserRewards getBalance(String userId) {
        return userRewardsMap.computeIfAbsent(
                userId, id -> new UserRewards(id, 500)
        );
    }

    public UserRewards earnPoints(String userId, BigDecimal transactionAmount) {
        // Calculate points earned - $1 = 1 point, rounded down
        int pointsEarned = transactionAmount.multiply(BigDecimal.valueOf(POINTS_PER_DOLLAR))
                .setScale(0, RoundingMode.DOWN)
                .intValue();
        
        UserRewards userRewards = userRewardsMap.computeIfAbsent(
                userId, id -> new UserRewards(id, 500)
        );
        userRewards.setPoints(userRewards.getPoints() + pointsEarned);
        return userRewards;
    }

    public UserRewards redeemPoints(String userId, int pointsToRedeem) {
        UserRewards userRewards = userRewardsMap.computeIfAbsent(
                userId, id -> new UserRewards(id, 500)
        );
        
        if (userRewards.getPoints() >= pointsToRedeem) {
            userRewards.setPoints(userRewards.getPoints() - pointsToRedeem);
            return userRewards;
        }
        return null; // Indicates insufficient points
    }
}

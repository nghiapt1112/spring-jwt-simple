package com.example.demo.reward.strategy;

import org.springframework.stereotype.Component;

/**
 * Premium implementation of point calculation strategy
 * Uses a higher rate of 15 points per currency unit
 */
@Component
public class PremiumPointCalculationStrategy implements PointCalculationStrategy {
    
    // Premium rate: 15 points per currency unit
    private static final int POINTS_PER_CURRENCY_UNIT = 15;
    
    @Override
    public int calculatePoints(double transactionAmount) {
        return (int) (transactionAmount * POINTS_PER_CURRENCY_UNIT);
    }
}

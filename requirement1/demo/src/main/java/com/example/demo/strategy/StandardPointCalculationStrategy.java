package com.example.demo.strategy;

import org.springframework.stereotype.Component;

/**
 * Standard implementation of point calculation strategy
 * Uses the standard rate of 10 points per currency unit
 */
@Component
public class StandardPointCalculationStrategy implements PointCalculationStrategy {
    
    // Standard rate: 10 points per currency unit
    private static final int POINTS_PER_CURRENCY_UNIT = 10;
    
    @Override
    public int calculatePoints(double transactionAmount) {
        return (int) (transactionAmount * POINTS_PER_CURRENCY_UNIT);
    }
}

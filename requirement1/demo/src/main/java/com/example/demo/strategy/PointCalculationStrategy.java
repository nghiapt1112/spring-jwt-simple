package com.example.demo.strategy;

/**
 * Strategy interface for calculating reward points
 */
public interface PointCalculationStrategy {
    
    /**
     * Calculate points earned from a transaction
     * @param transactionAmount Transaction amount
     * @return Points earned
     */
    int calculatePoints(double transactionAmount);
}

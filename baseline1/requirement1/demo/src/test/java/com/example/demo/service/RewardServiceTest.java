package com.example.demo.service;

import com.example.demo.config.InsufficientPointsException;
import com.example.demo.config.InvalidTransactionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RewardServiceTest {

    private RewardService rewardService;
    private final String testUserId = "testUser";

    @BeforeEach
    void setUp() {
        rewardService = new RewardService();
    }

    @Test
    void testEarnPoints() {
        // Test earning points with valid transaction
        int pointsEarned = rewardService.earnPoints(testUserId, 100.0);
        assertEquals(1000, pointsEarned); // 100 * 10 points
        assertEquals(1500, rewardService.getBalance(testUserId)); // 500 initial + 1000 earned
    }

    @Test
    void testEarnPointsWithInvalidAmount() {
        // Test with negative amount
        assertThrows(InvalidTransactionException.class, () -> 
            rewardService.earnPoints(testUserId, -50.0));
        
        // Test with zero amount
        assertThrows(InvalidTransactionException.class, () -> 
            rewardService.earnPoints(testUserId, 0.0));
    }

    @Test
    void testRedeemPoints() {
        // Ensure user exists with initial balance
        assertTrue(rewardService.earnPoints(testUserId, 10.0) > 0);
        
        // Test successful redemption
        assertTrue(rewardService.redeemPoints(testUserId, 100));
        assertEquals(500, rewardService.getBalance(testUserId)); // 500 initial + 100 earned - 100 redeemed
    }

    @Test
    void testRedeemPointsInsufficientBalance() {
        // Ensure user exists with initial balance
        assertTrue(rewardService.earnPoints(testUserId, 10.0) > 0);
        
        // Test insufficient balance
        assertThrows(InsufficientPointsException.class, () -> 
            rewardService.redeemPoints(testUserId, 1000));
    }

    @Test
    void testRedeemPointsInvalidAmount() {
        // Ensure user exists
        assertTrue(rewardService.earnPoints(testUserId, 10.0) > 0);
        
        // Test with negative amount
        assertThrows(InvalidTransactionException.class, () -> 
            rewardService.redeemPoints(testUserId, -50));
        
        // Test with zero amount
        assertThrows(InvalidTransactionException.class, () -> 
            rewardService.redeemPoints(testUserId, 0));
    }

    @Test
    void testGetBalance() {
        // Test with new user (should create automatically with 500 points)
        assertEquals(500, rewardService.getBalance("newUser"));
        
        // Test after earning points
        rewardService.earnPoints("newUser", 50.0);
        assertEquals(1000, rewardService.getBalance("newUser")); // 500 initial + 500 earned
    }

    @Test
    void testUserExists() {
        // Initial user should not exist
        assertFalse(rewardService.userExists("nonExistentUser"));
        
        // After getting balance, user should exist
        rewardService.getBalance("nonExistentUser");
        assertTrue(rewardService.userExists("nonExistentUser"));
    }
}

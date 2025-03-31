package com.example.demo.service;

import com.example.demo.infrastructure.exception.InvalidTransactionException;
import com.example.demo.infrastructure.exception.SecurityException;
import com.example.demo.model.PointTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Secure wrapper around RewardService that enforces identity verification
 * before allowing transactions
 */
@Service
@Primary // This will be used instead of the original RewardService
public class SecureRewardService {
    private static final Logger log = LoggerFactory.getLogger(SecureRewardService.class);
    
    private final RewardService rewardService;
    private final TransactionSecurityService securityService;
    
    @Autowired
    public SecureRewardService(
            RewardService rewardService, 
            TransactionSecurityService securityService) {
        this.rewardService = rewardService;
        this.securityService = securityService;
    }
    
    /**
     * Securely earn points from a transaction with identity verification
     * 
     * @param userId User ID
     * @param transactionAmount Transaction amount
     * @param deviceInfo Optional device information for additional security checks
     * @return Points earned
     * @throws InvalidTransactionException if transaction is invalid
     * @throws SecurityException if identity verification fails
     */
    public int earnPoints(
            String userId, 
            double transactionAmount, 
            Map<String, String> deviceInfo) {
        
        try {
            // Verify user identity before allowing transaction
            securityService.verifyTransactionSecurity(
                    userId, 
                    transactionAmount, 
                    "EARN_POINTS", 
                    deviceInfo);
            
            // If verification is successful, proceed with the transaction
            return rewardService.earnPoints(userId, transactionAmount);
        } catch (SecurityException e) {
            log.warn("Security verification failed for earnPoints: {} - {}", 
                    e.getErrorCode(), e.getMessage());
            throw e;
        }
    }
    
    /**
     * Convenience method for earning points without device info
     * 
     * @param userId User ID
     * @param transactionAmount Transaction amount
     * @return Points earned
     */
    public int earnPoints(String userId, double transactionAmount) {
        return earnPoints(userId, transactionAmount, new HashMap<>());
    }
    
    /**
     * Securely redeem points with identity verification
     * 
     * @param userId User ID
     * @param pointsToRedeem Points to redeem
     * @param deviceInfo Optional device information for additional security checks
     * @return true if redeemed successfully
     * @throws InvalidTransactionException if transaction is invalid
     * @throws SecurityException if identity verification fails
     */
    public boolean redeemPoints(
            String userId, 
            int pointsToRedeem, 
            Map<String, String> deviceInfo) {
        
        try {
            // Calculate equivalent transaction amount (for risk assessment)
            double equivalentAmount = pointsToRedeem * 0.01; // Example conversion
            
            // Verify user identity before allowing redemption
            securityService.verifyTransactionSecurity(
                    userId, 
                    equivalentAmount, 
                    "REDEEM_POINTS", 
                    deviceInfo);
            
            // If verification is successful, proceed with the redemption
            return rewardService.redeemPoints(userId, pointsToRedeem);
        } catch (SecurityException e) {
            log.warn("Security verification failed for redeemPoints: {} - {}", 
                    e.getErrorCode(), e.getMessage());
            throw e;
        }
    }
    
    /**
     * Convenience method for redeeming points without device info
     * 
     * @param userId User ID
     * @param pointsToRedeem Points to redeem
     * @return true if redeemed successfully
     */
    public boolean redeemPoints(String userId, int pointsToRedeem) {
        return redeemPoints(userId, pointsToRedeem, new HashMap<>());
    }
    
    /**
     * Verify an MFA code for a pending transaction
     * 
     * @param userId User ID
     * @param verificationCode Verification code to verify
     * @return true if verification successful
     * @throws SecurityException if verification fails
     */
    public boolean verifyMfaCode(String userId, String verificationCode) {
        return securityService.verifyMfaCode(userId, verificationCode);
    }
    
    /**
     * Get the current balance of points for a user
     * (No security verification needed for query operations)
     * 
     * @param userId User ID
     * @return Point balance
     */
    public int getBalance(String userId) {
        return rewardService.getBalance(userId);
    }
    
    /**
     * Check if a user exists
     * (No security verification needed for query operations)
     * 
     * @param userId User ID
     * @return true if the user exists
     */
    public boolean userExists(String userId) {
        return rewardService.userExists(userId);
    }
    
    /**
     * Get transaction history for a user
     * (No security verification needed for query operations)
     * 
     * @param userId User ID
     * @return List of transactions
     */
    public List<PointTransaction> getTransactionHistory(String userId) {
        return rewardService.getTransactionHistory(userId);
    }
}

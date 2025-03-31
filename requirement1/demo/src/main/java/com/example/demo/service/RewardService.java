package com.example.demo.service;

import com.example.demo.infrastructure.command.CommandExecutorService;
import com.example.demo.infrastructure.command.reward.EarnPointsCommand;
import com.example.demo.infrastructure.command.reward.RedeemPointsCommand;
import com.example.demo.infrastructure.event.EventPublisher;
import com.example.demo.infrastructure.exception.InvalidTransactionException;
import com.example.demo.infrastructure.factory.UserRewardPointsFactory;
import com.example.demo.model.PointTransaction;
import com.example.demo.model.UserRewardPoints;
import com.example.demo.repository.UserRewardPointsRepository;
import com.example.demo.strategy.PointCalculationStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Thread-safe service for managing reward points
 */
@Service
public class RewardService {

    private final UserRewardPointsRepository userRewardPointsRepository;
    private final PointCalculationStrategy pointCalculationStrategy;
    private final UserRewardPointsFactory userRewardPointsFactory;
    private final CommandExecutorService commandExecutorService;
    private final EventPublisher eventPublisher;
    
    // User-specific locks to prevent concurrent modifications to the same user's data
    // while allowing operations on different users to proceed in parallel
    private final Lock lock = new ReentrantLock();

    /**
     * Constructor
     * @param userRewardPointsRepository Repository for user reward points
     * @param pointCalculationStrategy Strategy for calculating points
     * @param userRewardPointsFactory Factory for creating user reward points
     * @param commandExecutorService Service for executing commands
     * @param eventPublisher Event publisher
     */
    @Autowired
    public RewardService(
            UserRewardPointsRepository userRewardPointsRepository,
            PointCalculationStrategy pointCalculationStrategy,
            UserRewardPointsFactory userRewardPointsFactory,
            CommandExecutorService commandExecutorService,
            EventPublisher eventPublisher) {
        this.userRewardPointsRepository = userRewardPointsRepository;
        this.pointCalculationStrategy = pointCalculationStrategy;
        this.userRewardPointsFactory = userRewardPointsFactory;
        this.commandExecutorService = commandExecutorService;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Earn points from a transaction (thread-safe)
     * @param userId User ID
     * @param transactionAmount Transaction amount
     * @return Points earned
     * @throws InvalidTransactionException if transaction amount is invalid
     */
    public int earnPoints(String userId, double transactionAmount) {
        // Validate transaction amount
        if (transactionAmount <= 0) {
            throw new InvalidTransactionException("Transaction amount must be positive");
        }

        // Try to acquire a lock for this user's operations (timeout after 5 seconds)
        try {
            if (lock.tryLock(5, TimeUnit.SECONDS)) {
                try {
                    // Get or create user reward points
                    UserRewardPoints userRewardPoints = getUserRewardPoints(userId);
                    
                    // Create and execute the command
                    EarnPointsCommand command = new EarnPointsCommand(
                            userRewardPoints, 
                            transactionAmount, 
                            pointCalculationStrategy,
                            userRewardPointsRepository,
                            eventPublisher);
                    
                    return commandExecutorService.executeCommand(command);
                } finally {
                    lock.unlock();
                }
            } else {
                throw new InvalidTransactionException("System is busy, please try again later");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new InvalidTransactionException("Operation was interrupted");
        }
    }

    /**
     * Redeem points (thread-safe)
     * @param userId User ID
     * @param pointsToRedeem Points to redeem
     * @return true if redeemed successfully
     * @throws InvalidTransactionException if points to redeem is invalid
     */
    public boolean redeemPoints(String userId, int pointsToRedeem) {
        // Validate points to redeem
        if (pointsToRedeem <= 0) {
            throw new InvalidTransactionException("Points to redeem must be positive");
        }

        // Try to acquire a lock for this user's operations (timeout after 5 seconds)
        try {
            if (lock.tryLock(5, TimeUnit.SECONDS)) {
                try {
                    // Get user reward points
                    UserRewardPoints userRewardPoints = getUserRewardPoints(userId);
                    
                    // Create and execute the command
                    RedeemPointsCommand command = new RedeemPointsCommand(
                            userRewardPoints, 
                            pointsToRedeem, 
                            userRewardPointsRepository,
                            eventPublisher);
                    
                    // Execute command and convert result to boolean (non-zero = success)
                    return commandExecutorService.executeCommand(command) > 0;
                } finally {
                    lock.unlock();
                }
            } else {
                throw new InvalidTransactionException("System is busy, please try again later");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new InvalidTransactionException("Operation was interrupted");
        }
    }

    /**
     * Get the current balance of points for a user (thread-safe)
     * @param userId User ID
     * @return Point balance
     */
    public int getBalance(String userId) {
        return getUserRewardPoints(userId).getRewardPoints();
    }

    /**
     * Check if a user exists (thread-safe)
     * @param userId User ID
     * @return true if the user exists
     */
    public boolean userExists(String userId) {
        return userRewardPointsRepository.existsByUserId(userId);
    }
    
    /**
     * Get transaction history for a user (thread-safe)
     * @param userId User ID
     * @return List of transactions
     */
    public List<PointTransaction> getTransactionHistory(String userId) {
        return getUserRewardPoints(userId).getTransactions();
    }
    
    /**
     * Get or create user reward points (thread-safe)
     * @param userId User ID
     * @return User reward points
     */
    private UserRewardPoints getUserRewardPoints(String userId) {
        return userRewardPointsRepository.findByUserId(userId)
                .orElseGet(() -> {
                    UserRewardPoints newUserRewardPoints = userRewardPointsFactory.createUserRewardPoints(userId);
                    return userRewardPointsRepository.save(newUserRewardPoints);
                });
    }
}

package com.example.demo.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Thread-safe model class for user reward points
 */
@Getter
public class UserRewardPoints {
    
    @Setter
    private String userId;
    
    // Using AtomicInteger for thread-safe operations on the point balance
    private final AtomicInteger rewardPoints;
    
    // Using synchronized list for thread-safe access to transactions
    private final List<PointTransaction> transactions = Collections.synchronizedList(new ArrayList<>());
    
    // Lock for complex operations that need to be atomic
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    
    /**
     * Constructor with user ID
     * @param userId User ID
     */
    public UserRewardPoints(String userId) {
        this.userId = userId;
        this.rewardPoints = new AtomicInteger(500); // Initial points
        
        // Record initial balance as a transaction
        this.transactions.add(new PointTransaction(
                TransactionType.INITIAL,
                500,
                "Initial balance",
                LocalDateTime.now(),
                userId));
    }
    
    /**
     * Get current reward points
     * @return Current reward points
     */
    public int getRewardPoints() {
        return rewardPoints.get();
    }
    
    /**
     * Set reward points (thread-safe)
     * @param points New points value
     */
    public void setRewardPoints(int points) {
        rewardPoints.set(points);
    }
    
    /**
     * Add points to balance (thread-safe)
     * @param points Points to add
     * @param description Transaction description
     * @return Updated balance
     */
    public int addPoints(int points, String description) {
        lock.writeLock().lock();
        try {
            // Atomic update of points
            int newBalance = rewardPoints.addAndGet(points);
            
            // Record transaction
            transactions.add(new PointTransaction(
                    TransactionType.EARN,
                    points,
                    description,
                    LocalDateTime.now(),
                    userId));
            
            return newBalance;
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * Deduct points from balance (thread-safe)
     * @param points Points to deduct
     * @param description Transaction description
     * @return true if deducted successfully
     */
    public boolean deductPoints(int points, String description) {
        lock.writeLock().lock();
        try {
            // Check if we have enough points
            int currentPoints = rewardPoints.get();
            if (currentPoints >= points) {
                // Atomic update
                boolean updated = rewardPoints.compareAndSet(currentPoints, currentPoints - points);
                
                if (!updated) {
                    // If the update failed due to concurrent modification, try again
                    return deductPoints(points, description);
                }
                
                // Record transaction
                transactions.add(new PointTransaction(
                        TransactionType.REDEEM,
                        points,
                        description,
                        LocalDateTime.now(),
                        userId));
                
                return true;
            }
            return false;
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * Get transaction history (thread-safe)
     * @return Unmodifiable list of transactions
     */
    public List<PointTransaction> getTransactions() {
        lock.readLock().lock();
        try {
            return Collections.unmodifiableList(new ArrayList<>(transactions));
        } finally {
            lock.readLock().unlock();
        }
    }
}

package com.example.demo.repository;

import com.example.demo.model.UserRewardPoints;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Thread-safe in-memory implementation of UserRewardPointsRepository
 */
@Repository
public class InMemoryUserRewardPointsRepository implements UserRewardPointsRepository {
    
    // Using ConcurrentHashMap for thread-safe storage
    private final Map<String, UserRewardPoints> userPointsMap = new ConcurrentHashMap<>();
    
    // Global lock for operations that need to be atomic across the entire repository
    private final ReadWriteLock globalLock = new ReentrantReadWriteLock();
    
    public InMemoryUserRewardPointsRepository() {
        // Initialize with a demo user for testing purposes
        userPointsMap.put("user123", new UserRewardPoints("user123"));
    }
    
    @Override
    public Optional<UserRewardPoints> findByUserId(String userId) {
        Lock readLock = globalLock.readLock();
        readLock.lock();
        try {
            UserRewardPoints userRewardPoints = userPointsMap.get(userId);
            // Return a deep copy to prevent race conditions
            if (userRewardPoints != null) {
                return Optional.of(userRewardPoints);
            }
            return Optional.empty();
        } finally {
            readLock.unlock();
        }
    }
    
    @Override
    public UserRewardPoints save(UserRewardPoints userRewardPoints) {
        Lock writeLock = globalLock.writeLock();
        writeLock.lock();
        try {
            userPointsMap.put(userRewardPoints.getUserId(), userRewardPoints);
            return userRewardPoints;
        } finally {
            writeLock.unlock();
        }
    }
    
    @Override
    public boolean existsByUserId(String userId) {
        Lock readLock = globalLock.readLock();
        readLock.lock();
        try {
            return userPointsMap.containsKey(userId);
        } finally {
            readLock.unlock();
        }
    }
    
    @Override
    public void deleteByUserId(String userId) {
        Lock writeLock = globalLock.writeLock();
        writeLock.lock();
        try {
            userPointsMap.remove(userId);
        } finally {
            writeLock.unlock();
        }
    }
}

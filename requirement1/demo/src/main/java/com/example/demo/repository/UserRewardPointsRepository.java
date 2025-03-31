package com.example.demo.repository;

import com.example.demo.model.UserRewardPoints;

import java.util.Optional;

/**
 * Repository interface for managing user reward points
 */
public interface UserRewardPointsRepository {
    
    /**
     * Find user reward points by user ID
     * @param userId User ID
     * @return Optional containing user reward points if found
     */
    Optional<UserRewardPoints> findByUserId(String userId);
    
    /**
     * Save user reward points
     * @param userRewardPoints User reward points to save
     * @return Saved user reward points
     */
    UserRewardPoints save(UserRewardPoints userRewardPoints);
    
    /**
     * Check if user exists
     * @param userId User ID
     * @return true if user exists
     */
    boolean existsByUserId(String userId);
    
    /**
     * Delete user reward points
     * @param userId User ID
     */
    void deleteByUserId(String userId);
}

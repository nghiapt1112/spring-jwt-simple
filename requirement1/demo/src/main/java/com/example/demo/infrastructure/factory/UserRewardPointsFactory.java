package com.example.demo.infrastructure.factory;

import com.example.demo.reward.model.UserRewardPoints;

/**
 * Factory interface for creating UserRewardPoints objects
 */
public interface UserRewardPointsFactory {
    
    /**
     * Create a new UserRewardPoints object for the specified user ID
     * @param userId User ID
     * @return New UserRewardPoints object
     */
    UserRewardPoints createUserRewardPoints(String userId);
}

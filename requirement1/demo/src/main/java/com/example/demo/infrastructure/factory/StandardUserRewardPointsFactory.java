package com.example.demo.infrastructure.factory;

import com.example.demo.reward.model.UserRewardPoints;
import org.springframework.stereotype.Component;

/**
 * Standard implementation of UserRewardPointsFactory
 * Creates users with the default initial points balance
 */
@Component
public class StandardUserRewardPointsFactory implements UserRewardPointsFactory {
    
    // Default initial points for new users
    private static final int DEFAULT_INITIAL_POINTS = 500;
    
    @Override
    public UserRewardPoints createUserRewardPoints(String userId) {
        UserRewardPoints userRewardPoints = new UserRewardPoints(userId);
        userRewardPoints.setRewardPoints(DEFAULT_INITIAL_POINTS);
        return userRewardPoints;
    }
}

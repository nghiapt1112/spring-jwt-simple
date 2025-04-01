package com.example.demo.infrastructure.command.reward;

import com.example.demo.infrastructure.command.Command;
import com.example.demo.infrastructure.command.CommandTypes;
import com.example.demo.infrastructure.event.EventPublisher;
import com.example.demo.infrastructure.event.EventTypes;
import com.example.demo.infrastructure.event.reward.RewardEventData;
import com.example.demo.infrastructure.exception.InsufficientPointsException;
import com.example.demo.reward.model.UserRewardPoints;
import com.example.demo.reward.repository.UserRewardPointsRepository;

/**
 * Command to redeem reward points
 */
public class RedeemPointsCommand implements Command<Integer> {
    
    private final UserRewardPoints userRewardPoints;
    private final int pointsToRedeem;
    private final UserRewardPointsRepository repository;
    private final EventPublisher eventPublisher;
    
    /**
     * Constructor
     * @param userRewardPoints User reward points
     * @param pointsToRedeem Points to redeem
     * @param repository Repository for persistence
     * @param eventPublisher Event publisher
     */
    public RedeemPointsCommand(
            UserRewardPoints userRewardPoints,
            int pointsToRedeem,
            UserRewardPointsRepository repository,
            EventPublisher eventPublisher) {
        this.userRewardPoints = userRewardPoints;
        this.pointsToRedeem = pointsToRedeem;
        this.repository = repository;
        this.eventPublisher = eventPublisher;
    }
    
    @Override
    public Integer execute() {
        // Check if user has enough points
        if (userRewardPoints.getRewardPoints() < pointsToRedeem) {
            throw new InsufficientPointsException(
                    String.format("User %s has only %d points but tried to redeem %d points",
                            userRewardPoints.getUserId(),
                            userRewardPoints.getRewardPoints(),
                            pointsToRedeem));
        }
        
        // Deduct points
        boolean success = userRewardPoints.deductPoints(pointsToRedeem, getDescription());
        
        if (!success) {
            throw new InsufficientPointsException("Failed to deduct points");
        }
        
        // Save changes
        repository.save(userRewardPoints);
        
        // Publish event
        if (eventPublisher != null) {
            RewardEventData eventData = new RewardEventData(
                    pointsToRedeem,
                    userRewardPoints);
            
            eventPublisher.publishEvent(
                    EventTypes.POINTS_REDEEMED,
                    userRewardPoints.getUserId(),
                    eventData);
        }
        
        return pointsToRedeem;
    }
    
    @Override
    public String getCommandType() {
        return CommandTypes.REDEEM_POINTS;
    }
    
    @Override
    public String getDescription() {
        return String.format("Redeemed %d points", pointsToRedeem);
    }
}

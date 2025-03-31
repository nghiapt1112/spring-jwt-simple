package com.example.demo.infrastructure.command.reward;

import com.example.demo.infrastructure.command.Command;
import com.example.demo.infrastructure.command.CommandTypes;
import com.example.demo.infrastructure.event.EventPublisher;
import com.example.demo.infrastructure.event.EventTypes;
import com.example.demo.infrastructure.event.reward.RewardEventData;
import com.example.demo.model.UserRewardPoints;
import com.example.demo.repository.UserRewardPointsRepository;
import com.example.demo.strategy.PointCalculationStrategy;

/**
 * Command to earn reward points
 */
public class EarnPointsCommand implements Command<Integer> {
    
    private final UserRewardPoints userRewardPoints;
    private final double transactionAmount;
    private final PointCalculationStrategy calculationStrategy;
    private final UserRewardPointsRepository repository;
    private final EventPublisher eventPublisher;
    
    /**
     * Constructor
     * @param userRewardPoints User reward points
     * @param transactionAmount Transaction amount
     * @param calculationStrategy Calculation strategy
     * @param repository Repository for persistence
     * @param eventPublisher Event publisher
     */
    public EarnPointsCommand(
            UserRewardPoints userRewardPoints, 
            double transactionAmount, 
            PointCalculationStrategy calculationStrategy,
            UserRewardPointsRepository repository,
            EventPublisher eventPublisher) {
        this.userRewardPoints = userRewardPoints;
        this.transactionAmount = transactionAmount;
        this.calculationStrategy = calculationStrategy;
        this.repository = repository;
        this.eventPublisher = eventPublisher;
    }
    
    @Override
    public Integer execute() {
        // Calculate points to earn
        int pointsEarned = calculationStrategy.calculatePoints(transactionAmount);
        
        // Add points to user's account
        userRewardPoints.addPoints(pointsEarned, getDescription());
        
        // Save changes
        repository.save(userRewardPoints);
        
        // Publish event
        if (eventPublisher != null) {
            RewardEventData eventData = new RewardEventData(
                    pointsEarned,
                    userRewardPoints);
            
            eventPublisher.publishEvent(
                    EventTypes.POINTS_EARNED,
                    userRewardPoints.getUserId(),
                    eventData);
        }
        
        return pointsEarned;
    }
    
    @Override
    public String getCommandType() {
        return CommandTypes.EARN_POINTS;
    }
    
    @Override
    public String getDescription() {
        return String.format("Earned points from transaction amount: %.2f", transactionAmount);
    }
}

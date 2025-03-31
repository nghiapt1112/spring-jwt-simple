package com.example.demo.infrastructure.event.reward;

import com.example.demo.infrastructure.event.Event;
import com.example.demo.infrastructure.event.EventListener;
import com.example.demo.infrastructure.event.EventTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Listener for reward events that logs them
 */
@Component
public class LoggingRewardEventListener implements EventListener<Event<RewardEventData>> {
    
    private static final Logger logger = LoggerFactory.getLogger(LoggingRewardEventListener.class);
    
    @Override
    public void onEvent(Event<RewardEventData> event) {
        RewardEventData data = event.getEventData();
        String userId = event.getUserId();
        int points = data.getPoints();
        int newBalance = data.getUserRewardPoints().getRewardPoints();
        
        switch (event.getEventType()) {
            case EventTypes.POINTS_EARNED:
                logger.info("User {} earned {} points. New balance: {}", userId, points, newBalance);
                break;
            case EventTypes.POINTS_REDEEMED:
                logger.info("User {} redeemed {} points. New balance: {}", userId, points, newBalance);
                break;
            default:
                logger.info("Unhandled reward event type: {}", event.getEventType());
        }
    }
    
    @Override
    public boolean supportsEventType(String eventType) {
        return eventType.equals(EventTypes.POINTS_EARNED) || 
               eventType.equals(EventTypes.POINTS_REDEEMED);
    }
}

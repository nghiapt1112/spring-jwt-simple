package com.example.demo.infrastructure.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Event publisher for all system events
 */
@Component
public class EventPublisher {
    
    private final List<EventListener<?>> eventListeners;
    
    @Autowired
    public EventPublisher(List<EventListener<?>> eventListeners) {
        this.eventListeners = eventListeners;
    }
    
    /**
     * Create and publish an event
     * @param eventType The type of event
     * @param userId The user ID
     * @param data The event data
     * @param <T> The type of event data
     * @return The created event
     */
    public <T> Event<T> publishEvent(String eventType, String userId, T data) {
        // Create event with a unique ID
        Event<T> event = new Event<>(
                UUID.randomUUID().toString(),
                eventType,
                userId,
                data
        );
        
        // Publish the event to all supporting listeners
        publishEvent(event);
        
        return event;
    }
    
    /**
     * Publish an existing event
     * @param event The event to publish
     * @param <T> The type of event data
     */
    @SuppressWarnings("unchecked")
    public <T> void publishEvent(Event<T> event) {
        for (EventListener<?> listener : eventListeners) {
            if (listener.supportsEventType(event.getEventType())) {
                ((EventListener<Event<T>>)listener).onEvent(event);
            }
        }
    }
}

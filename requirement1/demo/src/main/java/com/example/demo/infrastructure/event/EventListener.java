package com.example.demo.infrastructure.event;

/**
 * Generic event listener interface
 * @param <T> The type of event
 */
public interface EventListener<T> {
    
    /**
     * Handle an event
     * @param event The event to handle
     */
    void onEvent(T event);
    
    /**
     * Check if this listener supports the given event type
     * @param eventType The event type to check
     * @return true if this listener supports the event type
     */
    boolean supportsEventType(String eventType);
}

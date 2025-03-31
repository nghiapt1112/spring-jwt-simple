package com.example.demo.infrastructure.event;

/**
 * Constants for event types
 */
public final class EventTypes {
    
    // Prevent instantiation
    private EventTypes() {}
    
    // Reward events
    public static final String POINTS_EARNED = "POINTS_EARNED";
    public static final String POINTS_REDEEMED = "POINTS_REDEEMED";
    
    // Add other event types here as needed
    // public static final String USER_REGISTERED = "USER_REGISTERED";
    // public static final String PAYMENT_PROCESSED = "PAYMENT_PROCESSED";
}

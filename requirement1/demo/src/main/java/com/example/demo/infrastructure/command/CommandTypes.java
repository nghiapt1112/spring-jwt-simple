package com.example.demo.infrastructure.command;

/**
 * Constants for command types
 */
public final class CommandTypes {
    
    // Prevent instantiation
    private CommandTypes() {}
    
    // Reward commands
    public static final String EARN_POINTS = "EARN_POINTS";
    public static final String REDEEM_POINTS = "REDEEM_POINTS";
    
    // Add other command types here as needed
    // public static final String CREATE_USER = "CREATE_USER";
    // public static final String PROCESS_PAYMENT = "PROCESS_PAYMENT";
}

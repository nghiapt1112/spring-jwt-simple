package com.example.demo.infrastructure.command;

/**
 * Generic command interface for all system commands
 * @param <T> The result type of the command
 */
public interface Command<T> {
    
    /**
     * Execute the command
     * @return The result of the command
     */
    T execute();
    
    /**
     * Get the command type
     * @return The command type
     */
    String getCommandType();
    
    /**
     * Get the description of this command instance
     * @return The command description
     */
    String getDescription();
}

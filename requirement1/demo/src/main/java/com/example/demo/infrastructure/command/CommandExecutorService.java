package com.example.demo.infrastructure.command;

import org.springframework.stereotype.Service;

/**
 * Generic command executor service for all system commands
 */
@Service
public class CommandExecutorService {
    
    /**
     * Execute a command
     * @param command The command to execute
     * @param <T> The result type of the command
     * @return The result of the command execution
     */
    public <T> T executeCommand(Command<T> command) {
        // Record command execution (could add logging, metrics, etc.)
        
        // Execute the command
        return command.execute();
    }
}

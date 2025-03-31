package com.example.demo.infrastructure;

import org.springframework.context.annotation.Configuration;

/**
 * Configuration for event listeners
 * In the new architecture, event listeners are automatically discovered 
 * and registered by Spring when they're annotated with @Component
 */
@Configuration
public class EventListenerConfig {
    
    // Empty configuration class - serves as documentation that listeners
    // are automatically registered by Spring's component scanning
}

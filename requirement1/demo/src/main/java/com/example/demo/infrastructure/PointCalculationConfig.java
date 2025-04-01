package com.example.demo.infrastructure;

import com.example.demo.reward.strategy.PointCalculationStrategy;
import com.example.demo.reward.strategy.PremiumPointCalculationStrategy;
import com.example.demo.reward.strategy.StandardPointCalculationStrategy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Configuration for point calculation strategies
 */
@Configuration
public class PointCalculationConfig {
    
    @Value("${reward.calculation.strategy:standard}")
    private String calculationStrategy;
    
    /**
     * Select the appropriate point calculation strategy based on configuration
     * @param standardStrategy Standard strategy
     * @param premiumStrategy Premium strategy
     * @return Selected strategy
     */
    @Bean
    @Primary
    public PointCalculationStrategy pointCalculationStrategy(
            StandardPointCalculationStrategy standardStrategy,
            PremiumPointCalculationStrategy premiumStrategy) {
        
        // Select strategy based on configuration
        if ("premium".equalsIgnoreCase(calculationStrategy)) {
            return premiumStrategy;
        }
        
        // Default to standard strategy
        return standardStrategy;
    }
}

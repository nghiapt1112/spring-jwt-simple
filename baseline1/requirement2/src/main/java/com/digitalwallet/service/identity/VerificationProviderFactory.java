package com.digitalwallet.service.identity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Factory for creating verification providers based on verification level.
 * Part of the Factory Method Pattern implementation.
 */
@Component
public class VerificationProviderFactory {
    @Autowired
    private StandardVerificationProvider standardProvider;
    
    @Autowired
    private EnhancedVerificationProvider enhancedProvider;
    
    @Autowired
    private BiometricVerificationProvider biometricProvider;
    
    /**
     * Get the appropriate verification provider based on the verification level.
     */
    public IdentityVerificationProvider getProvider(VerificationLevel level) {
        switch (level) {
            case LOW:
                return standardProvider;
            case MEDIUM:
                return enhancedProvider;
            case HIGH:
                return biometricProvider;
            default:
                return standardProvider;
        }
    }
}
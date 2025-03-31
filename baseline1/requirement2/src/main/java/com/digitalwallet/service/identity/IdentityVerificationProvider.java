package com.digitalwallet.service.identity;

import com.digitalwallet.exception.IdentityVerificationException;
import com.digitalwallet.model.VerificationRequest;
import com.digitalwallet.model.VerificationResult;

/**
 * Interface for identity verification providers.
 * Part of the Strategy Pattern implementation.
 */
public interface IdentityVerificationProvider {
    /**
     * Verify the user's identity.
     * 
     * @param request The verification request
     * @param apiCaller The component that will make the API call
     * @return The verification result
     * @throws IdentityVerificationException if verification fails
     */
    VerificationResult verify(VerificationRequest request, ApiCaller apiCaller) throws IdentityVerificationException;
}
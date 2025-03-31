package com.digitalwallet.verification.handler;

import com.digitalwallet.service.VerificationApiResult;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;

/**
 * Input validation handler for verifying user data integrity.
 */
@Slf4j
public class InputValidationHandler extends AbstractVerificationHandler {
    @Override
    public CompletableFuture<VerificationApiResult> verify(VerificationContext context) {
        context.addProcessedStep("Input Validation");
        
        // Validate input data
        if (context.getUserData() == null || 
            context.getUserData().getUserId() == null || 
            context.getUserData().getUserId().isEmpty() ||
            context.getUserData().getFullName() == null || 
            context.getUserData().getFullName().isEmpty()) {
            
            context.addFailureReason("Invalid or missing user data");
            log.warn("Input validation failed: Invalid user data");
            
            return CompletableFuture.completedFuture(
                createFailedVerification("INVALID_INPUT", "Invalid user data")
            );
        }
        
        return processNext(context);
    }
}

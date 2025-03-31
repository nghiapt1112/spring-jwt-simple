package com.digitalwallet.verification.handler;

import com.digitalwallet.service.VerificationApiResult;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;

/**
 * Final API verification handler that simulates the third-party verification service.
 */
@Slf4j
public class ApiVerificationHandler extends AbstractVerificationHandler {
    @Override
    public CompletableFuture<VerificationApiResult> verify(VerificationContext context) {
        context.addProcessedStep("API Verification");

        // Simulate network latency
        simulateNetworkLatency();

        // Check if any previous steps have failed
        if (!context.getFailureReasons().isEmpty()) {
            log.warn("Verification failed in previous steps. Reasons: {}", context.getFailureReasons());
            return CompletableFuture.completedFuture(
                createFailedVerification(
                    context.getFailureReasons().get(0),
                    "Verification failed in previous steps"
                )
            );
        }

        // Final verification logic
        if (isTestAccount(context)) {
            log.warn("Test account verification failed");
            return CompletableFuture.completedFuture(
                createFailedVerification("TEST_ACCOUNT", "Test accounts cannot be verified")
            );
        }

        // If all checks pass, return successful verification
        log.info("API verification successful for user: {}", context.getUserData().getUserId());
        return CompletableFuture.completedFuture(createSuccessfulVerification());
    }

    /**
     * Simulate network latency to mimic real-world API call.
     */
    private void simulateNetworkLatency() {
        try {
            // Random delay between 200-800 milliseconds
            Thread.sleep((long) (Math.random() * 600 + 200));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Network latency simulation interrupted");
        }
    }

    /**
     * Check if the account is a test account.
     *
     * @param context Verification context
     * @return true if it's a test account, false otherwise
     */
    private boolean isTestAccount(VerificationContext context) {
        return context.getUserData().getUserId().toLowerCase().contains("test")
            || context.getUserData().getFullName().toLowerCase().contains("test");
    }
}

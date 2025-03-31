package com.digitalwallet.service;

import com.digitalwallet.verification.handler.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Service for verifying user identity before allowing transactions in a digital wallet system.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class IdentityVerificationService {

    /**
     * Create a verification handler chain.
     * 
     * @return First handler in the verification chain
     */
    private VerificationHandler createVerificationChain() {
        // Configure the handler chain
        InputValidationHandler inputValidation = new InputValidationHandler();
        MfaVerificationHandler mfaVerification = new MfaVerificationHandler();
        DataEncryptionHandler dataEncryption = new DataEncryptionHandler();
//        TransactionLimitHandler transactionLimit = new TransactionLimitHandler();
//        ApiVerificationHandler apiVerification = new ApiVerificationHandler();

        // Chain the handlers
        inputValidation
            .setNextHandler(mfaVerification)
            .setNextHandler(dataEncryption)
//            .setNextHandler(transactionLimit)
//            .setNextHandler(apiVerification)
            ;

        return inputValidation;
    }

    /**
     * Verifies a user's identity before allowing transactions.
     *
     * @param userData User data containing verification information
     * @param options  Optional configuration parameters
     * @return Verification result with status and details
     */
    public CompletableFuture<VerificationResult> verifyUserIdentity(
        UserData userData, 
        VerificationOptions options
    ) {
        try {
            // Create verification context
            VerificationContext context = new VerificationContext(
                userData, 
                options, 
                new HashMap<>()
            );

            // Get the first handler in the chain
            VerificationHandler firstHandler = createVerificationChain();

            // Process the verification chain
            return firstHandler.verify(context)
                .thenApply(apiResult -> {
                    // Convert API result to Verification Result
                    if (apiResult.isSuccess()) {
                        return new VerificationResult(
                            "success",
                            "IDENTITY_VERIFIED",
                            null,
                            java.time.LocalDateTime.now().toString(),
                            apiResult.getVerificationId(),
                            apiResult.getExpiresAt()
                        );
                    } else {
                        return new VerificationResult(
                            "failed",
                            apiResult.getErrorCode(),
                            apiResult.getErrorReason(),
                            java.time.LocalDateTime.now().toString(),
                            null,
                            null
                        );
                    }
                })
                .exceptionally(ex -> {
                    // Log any unexpected errors
                    log.error("Unexpected error during identity verification", ex);
                    return new VerificationResult(
                        "failed",
                        "SYSTEM_ERROR",
                        "Unexpected error occurred during verification",
                        java.time.LocalDateTime.now().toString(),
                        null,
                        null
                    );
                });
        } catch (Exception e) {
            log.error("Error during identity verification", e);
            return CompletableFuture.completedFuture(
                new VerificationResult(
                    "failed",
                    "SYSTEM_ERROR",
                    "Error processing verification",
                    java.time.LocalDateTime.now().toString(),
                    null,
                    null
                )
            );
        }
    }
}

package com.digitalwallet.verification.handler;

import com.digitalwallet.service.VerificationApiResult;
import java.util.concurrent.CompletableFuture;
import java.util.UUID;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public abstract class AbstractVerificationHandler implements VerificationHandler {
    private VerificationHandler nextHandler;

    @Override
    public VerificationHandler setNextHandler(VerificationHandler handler) {
        this.nextHandler = handler;
        return this;
    }

    protected CompletableFuture<VerificationApiResult> processNext(VerificationContext context) {
        if (nextHandler != null) {
            return nextHandler.verify(context);
        }
        
        // If no more handlers, consider verification successful
        return CompletableFuture.completedFuture(
            createSuccessfulVerification()
        );
    }

    protected VerificationApiResult createSuccessfulVerification() {
        String verificationId = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now(ZoneOffset.UTC).plusHours(24);

        return new VerificationApiResult(
            true, 
            verificationId, 
            expiresAt.toString(), 
            null, 
            null
        );
    }

    protected VerificationApiResult createFailedVerification(String errorCode, String errorMessage) {
        return new VerificationApiResult(
            false, 
            null, 
            null, 
            errorCode, 
            errorMessage
        );
    }
}

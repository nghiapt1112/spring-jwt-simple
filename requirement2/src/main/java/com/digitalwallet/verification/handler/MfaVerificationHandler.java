package com.digitalwallet.verification.handler;

import com.digitalwallet.service.VerificationApiResult;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;
@Slf4j
public class MfaVerificationHandler extends AbstractVerificationHandler {
    @Override
    public CompletableFuture<VerificationApiResult> verify(VerificationContext context) {
        context.addProcessedStep("MFA Verification");
        
        String mfaCode = context.getUserData().getMfaCode();
        
        // Explicitly fail on invalid MFA code
        if (!isValidMfaCode(mfaCode)) {
            context.addFailureReason("Invalid MFA code");
            log.warn("MFA verification failed for user: {}", context.getUserData().getUserId());
            
            return CompletableFuture.completedFuture(
                createFailedVerification("INVALID_MFA", "Multi-factor authentication failed")
            );
        }
        
        return processNext(context);
    }

    private boolean isValidMfaCode(String mfaCode) {
        // Comprehensive MFA code validation
        return !(mfaCode == null || 
                 mfaCode.length() != 6 || 
                 !mfaCode.matches("\\d{6}") || 
                 mfaCode.equals("000000"));
    }
}

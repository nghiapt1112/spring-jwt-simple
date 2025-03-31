package com.digitalwallet.verification.handler;

import com.digitalwallet.service.UserData;
import com.digitalwallet.service.VerificationOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Context class to hold verification data and intermediate results.
 */
public class VerificationContext {
    private final UserData userData;
    private final VerificationOptions options;
    private final Map<String, String> encryptedData;
    private final List<String> processedSteps = new ArrayList<>();
    private final List<String> failureReasons = new ArrayList<>();

    /**
     * Constructor for VerificationContext.
     * 
     * @param userData User data for verification
     * @param options Verification options
     * @param encryptedData Encrypted sensitive data
     */
    public VerificationContext(
        UserData userData, 
        VerificationOptions options, 
        Map<String, String> encryptedData
    ) {
        this.userData = userData;
        this.options = options;
        this.encryptedData = encryptedData != null ? encryptedData : new HashMap<>();
    }

    /**
     * Get the user data.
     * 
     * @return UserData
     */
    public UserData getUserData() {
        return userData;
    }

    /**
     * Get verification options.
     * 
     * @return VerificationOptions
     */
    public VerificationOptions getOptions() {
        return options;
    }

    /**
     * Get encrypted data.
     * 
     * @return Map of encrypted data
     */
    public Map<String, String> getEncryptedData() {
        return encryptedData;
    }

    /**
     * Add a processed step to the context.
     * 
     * @param stepName Name of the processed step
     */
    public void addProcessedStep(String stepName) {
        processedSteps.add(stepName);
    }

    /**
     * Add a failure reason to the context.
     * 
     * @param reason Reason for verification failure
     */
    public void addFailureReason(String reason) {
        failureReasons.add(reason);
    }

    /**
     * Get list of processed steps.
     * 
     * @return List of processed step names
     */
    public List<String> getProcessedSteps() {
        return processedSteps;
    }

    /**
     * Get list of failure reasons.
     * 
     * @return List of failure reasons
     */
    public List<String> getFailureReasons() {
        return failureReasons;
    }
}

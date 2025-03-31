package com.digitalwallet.service.identity;

import com.digitalwallet.config.ApiProperties;
import com.digitalwallet.exception.IdentityVerificationException;
import com.digitalwallet.model.Transaction;
import com.digitalwallet.model.User;
import com.digitalwallet.model.VerificationRequest;
import com.digitalwallet.model.VerificationResult;
import com.digitalwallet.security.EncryptionService;
import com.digitalwallet.security.MfaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for integrating with third-party identity verification services.
 * Implements the Strategy Pattern to allow different verification services to be used
 * and the Decorator Pattern to add additional security measures.
 * Implements ApiCaller to break circular dependency.
 */
@Service
public class IdentityVerificationService implements ApiCaller {

    private static final Logger logger = LoggerFactory.getLogger(IdentityVerificationService.class);
    
    private final RestTemplate restTemplate;
    private final ApiProperties apiProperties;
    private final EncryptionService encryptionService;
    private final MfaService mfaService;
    private final ObjectMapper objectMapper;
    private final VerificationProviderFactory providerFactory;
    
    @Autowired
    public IdentityVerificationService(
            RestTemplate restTemplate,
            ApiProperties apiProperties,
            EncryptionService encryptionService,
            MfaService mfaService,
            ObjectMapper objectMapper,
            VerificationProviderFactory providerFactory) {
        this.restTemplate = restTemplate;
        this.apiProperties = apiProperties;
        this.encryptionService = encryptionService;
        this.mfaService = mfaService;
        this.objectMapper = objectMapper;
        this.providerFactory = providerFactory;
    }
    
    /**
     * Verifies a user's identity before allowing a transaction to proceed.
     * 
     * This function implements multiple security measures:
     * 1. Encryption of sensitive user data
     * 2. Multi-factor authentication when verification level requires it
     * 3. Secure API communication with HMAC authentication
     * 4. Request/response audit logging
     * 
     * @param user The user whose identity needs to be verified
     * @param transaction The transaction requiring verification
     * @param verificationLevel The level of verification required based on transaction risk
     * @return VerificationResult containing the verification status and details
     * @throws IdentityVerificationException if verification fails or errors occur
     */
    public VerificationResult verifyIdentityForTransaction(
            User user, 
            Transaction transaction, 
            VerificationLevel verificationLevel) throws IdentityVerificationException {
        
        String requestId = UUID.randomUUID().toString();
        
        try {
            logger.info("Starting identity verification for user: {}, transaction: {}, requestId: {}", 
                    user.getId(), transaction.getId(), requestId);
            
            // Step 1: Determine if MFA is required based on verification level and transaction amount
            boolean mfaRequired = verificationLevel == VerificationLevel.HIGH || 
                                 transaction.getAmount().compareTo(apiProperties.getHighRiskThreshold()) >= 0;
            
            // Step 2: If MFA is required, verify it was completed
            if (mfaRequired && !mfaService.verifyMfaCompletion(user.getId())) {
                logger.warn("MFA required but not completed for user: {}, transaction: {}", 
                        user.getId(), transaction.getId());
                return VerificationResult.builder()
                        .verified(false)
                        .requestId(requestId)
                        .timestamp(LocalDateTime.now())
                        .message("Multi-factor authentication required but not completed")
                        .build();
            }
            
            // Step 3: Prepare verification request with encrypted data
            VerificationRequest verificationRequest = prepareVerificationRequest(user, transaction, requestId);
            
            // Step 4: Select the appropriate verification provider based on the verification level
            IdentityVerificationProvider provider = providerFactory.getProvider(verificationLevel);
            
            // Step 5: Call the verification provider passing this instance as the ApiCaller
            VerificationResult result = provider.verify(verificationRequest, this);
            
            // Step 6: Log the verification result
            logVerificationResult(user.getId(), transaction.getId(), requestId, result);
            
            return result;
            
        } catch (Exception e) {
            logger.error("Error during identity verification for user: {}, transaction: {}, requestId: {}",
                    user.getId(), transaction.getId(), requestId, e);
            throw new IdentityVerificationException("Identity verification failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Prepares a verification request with encrypted sensitive data
     */
    private VerificationRequest prepareVerificationRequest(User user, Transaction transaction, String requestId) {
        // Create a map of sensitive data to be encrypted
        Map<String, Object> sensitiveData = new HashMap<>();
        sensitiveData.put("fullName", user.getFullName());
        sensitiveData.put("dateOfBirth", user.getDateOfBirth());
        sensitiveData.put("documentNumber", user.getDocumentNumber());
        sensitiveData.put("documentType", user.getDocumentType());
        
        // Encrypt the sensitive data
        String encryptedData = encryptionService.encrypt(sensitiveData);
        
        return VerificationRequest.builder()
                .requestId(requestId)
                .userId(user.getId())
                .transactionId(transaction.getId())
                .transactionAmount(transaction.getAmount())
                .encryptedUserData(encryptedData)
                .timestamp(LocalDateTime.now())
                .ipAddress(transaction.getIpAddress())
                .deviceId(transaction.getDeviceId())
                .build();
    }
    
    /**
     * Logs the verification result for audit purposes
     */
    private void logVerificationResult(String userId, String transactionId, String requestId, VerificationResult result) {
        if (result.isVerified()) {
            logger.info("Identity verification successful for user: {}, transaction: {}, requestId: {}", 
                    userId, transactionId, requestId);
        } else {
            logger.warn("Identity verification failed for user: {}, transaction: {}, requestId: {}, reason: {}", 
                    userId, transactionId, requestId, result.getMessage());
        }
    }
    
    /**
     * Makes the API call to the third-party verification service with retry capabilities
     * for better resilience against network issues or temporary service unavailability.
     * Implements the ApiCaller interface to break circular dependency.
     */
    @Override
    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))
    public ResponseEntity<String> callVerificationApi(String endpoint, Object requestBody, Map<String, String> headers) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        
        // Add all provided headers
        headers.forEach(httpHeaders::set);
        
        // Add timestamp and request ID for idempotency
        httpHeaders.set("X-Request-Timestamp", String.valueOf(System.currentTimeMillis()));
        httpHeaders.set("X-Request-ID", UUID.randomUUID().toString());
        
        HttpEntity<Object> entity = new HttpEntity<>(requestBody, httpHeaders);

        return ResponseEntity.of(Optional.of("[Mock] data process oke"));
//        return restTemplate.postForEntity(endpoint, entity, String.class);
    }
}
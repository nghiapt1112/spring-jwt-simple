package com.digitalwallet.service.identity;

import com.digitalwallet.model.VerificationRequest;
import com.digitalwallet.model.VerificationResult;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Biometric verification provider for highest security level checks.
 */
@Component
public class BiometricVerificationProvider extends BaseVerificationProvider {
    
    @Override
    protected Map<String, Object> prepareApiRequest(VerificationRequest request) {
        Map<String, Object> apiRequest = new HashMap<>();
        apiRequest.put("requestId", request.getRequestId());
        apiRequest.put("userId", request.getUserId());
        apiRequest.put("encryptedData", request.getEncryptedUserData());
        apiRequest.put("transactionId", request.getTransactionId());
        apiRequest.put("transactionAmount", request.getTransactionAmount());
        apiRequest.put("timestamp", request.getTimestamp().toString());
        apiRequest.put("ipAddress", request.getIpAddress());
        apiRequest.put("deviceId", request.getDeviceId());
        // Additional biometric data would be included here
        return apiRequest;
    }
    
    @Override
    protected String getApiEndpoint() {
        return apiProperties.getVerificationBaseUrl() + "/api/v1/biometric-verification";
    }
    
    @Override
    protected VerificationResult processResponse(ResponseEntity<String> response, VerificationRequest request) {
        // Implementation for biometric verification
        // Simulation for example purposes
        return VerificationResult.builder()
                .verified(true)
                .requestId(request.getRequestId())
                .providerId("BIOMETRIC")
                .timestamp(LocalDateTime.now())
                .message("Identity verified with biometric authentication")
                .build();
    }
    
    @Override
    protected VerificationResult postVerify(VerificationRequest request, VerificationResult result) {
        // For biometric verification, add additional fraud checks
        if (result.isVerified()) {
            logger.info("Performing additional fraud checks for high-risk transaction");
            // Additional fraud checks would be implemented here
        }
        return super.postVerify(request, result);
    }
}
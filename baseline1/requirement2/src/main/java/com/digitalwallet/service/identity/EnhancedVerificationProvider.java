package com.digitalwallet.service.identity;

import com.digitalwallet.model.VerificationRequest;
import com.digitalwallet.model.VerificationResult;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Enhanced verification provider for more detailed identity checks.
 */
@Component
public class EnhancedVerificationProvider extends BaseVerificationProvider {
    
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
        return apiRequest;
    }
    
    @Override
    protected String getApiEndpoint() {
        return apiProperties.getVerificationBaseUrl() + "/api/v1/enhanced-verification";
    }
    
    @Override
    protected VerificationResult processResponse(ResponseEntity<String> response, VerificationRequest request) {
        // Implementation similar to StandardVerificationProvider but with enhanced checks
        // Simulation for example purposes
        return VerificationResult.builder()
                .verified(true)
                .requestId(request.getRequestId())
                .providerId("ENHANCED")
                .timestamp(LocalDateTime.now())
                .message("Identity verified with enhanced checks")
                .build();
    }
}
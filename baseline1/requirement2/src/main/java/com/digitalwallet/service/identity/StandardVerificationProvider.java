package com.digitalwallet.service.identity;

import com.digitalwallet.model.VerificationRequest;
import com.digitalwallet.model.VerificationResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Standard verification provider for basic identity checks.
 */
@Component
public class StandardVerificationProvider extends BaseVerificationProvider {
    
    @Override
    protected Map<String, Object> prepareApiRequest(VerificationRequest request) {
        Map<String, Object> apiRequest = new HashMap<>();
        apiRequest.put("requestId", request.getRequestId());
        apiRequest.put("userId", request.getUserId());
        apiRequest.put("encryptedData", request.getEncryptedUserData());
        apiRequest.put("timestamp", request.getTimestamp().toString());
        return apiRequest;
    }
    
    @Override
    protected String getApiEndpoint() {
        return apiProperties.getVerificationBaseUrl() + "/api/v1/standard-verification";
    }
    
    @Override
    protected VerificationResult processResponse(ResponseEntity<String> response, VerificationRequest request) {
        try {
            // In a real implementation, parse the JSON response
            // For this example, we'll simulate a successful verification
            return VerificationResult.builder()
                    .verified(true)
                    .requestId(request.getRequestId())
                    .providerId("STANDARD")
                    .timestamp(LocalDateTime.now())
                    .message("Identity verified successfully")
                    .build();
        } catch (Exception e) {
            logger.error("Failed to process verification response", e);
            return VerificationResult.builder()
                    .verified(false)
                    .requestId(request.getRequestId())
                    .providerId("STANDARD")
                    .timestamp(LocalDateTime.now())
                    .message("Failed to process verification response: " + e.getMessage())
                    .build();
        }
    }
}
//package com.digitalwallet.integration;
//
//import com.digitalwallet.config.ApiProperties;
//import com.digitalwallet.model.Transaction;
//import com.digitalwallet.model.User;
//import com.digitalwallet.model.VerificationRequest;
//import com.digitalwallet.model.VerificationResult;
//import com.digitalwallet.security.EncryptionService;
//import com.digitalwallet.security.HmacUtils;
//import com.digitalwallet.security.MfaService;
//import com.digitalwallet.service.identity.BaseVerificationProvider;
//import com.digitalwallet.service.identity.IdentityVerificationService;
//import com.digitalwallet.service.identity.VerificationLevel;
//import com.digitalwallet.service.identity.VerificationProviderFactory;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.web.client.RestTemplate;
//
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//import java.util.HashMap;
//import java.util.Map;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.when;
//
//@ExtendWith(MockitoExtension.class)
//@ActiveProfiles("test")
//class IdentityVerificationIntegrationTest {
//
//    @Mock
//    private RestTemplate restTemplate;
//
//    @Mock
//    private ApiProperties apiProperties;
//
//    private EncryptionService encryptionService;
//    private MfaService mfaService;
//    private HmacUtils hmacUtils;
//    private ObjectMapper objectMapper;
//    private VerificationProviderFactory providerFactory;
//    private IdentityVerificationService verificationService;
//
//    private User user;
//    private Transaction transaction;
//
//    @BeforeEach
//    void setUp() {
//        objectMapper = new ObjectMapper();
//        hmacUtils = new HmacUtils();
//        mfaService = new MfaService();
//
//        // Configure API properties
//        when(apiProperties.getEncryptionSecret()).thenReturn("very-secure-secret-key-for-testing-only");
//        when(apiProperties.getVerificationBaseUrl()).thenReturn("https://api.example.com");
//        when(apiProperties.getVerificationApiKey()).thenReturn("test-api-key");
//        when(apiProperties.getVerificationApiSecret()).thenReturn("test-api-secret");
//        when(apiProperties.getHighRiskThreshold()).thenReturn(new BigDecimal("1000.00"));
//
//        encryptionService = new EncryptionService(apiProperties, objectMapper);
//
//        // Create test user
//        user = new User();
//        user.setId("user123");
//        user.setFullName("Jane Doe");
//        user.setEmail("jane.doe@example.com");
//        user.setDocumentNumber("A12345678");
//        user.setDocumentType("PASSPORT");
//        user.setMfaEnabled(true);
//        user.setVerified(true);
//
//        // Create test transaction
//        transaction = new Transaction();
//        transaction.setId("txn123");
//        transaction.setUserId("user123");
//        transaction.setAmount(new BigDecimal("500.00"));
//        transaction.setIpAddress("192.168.1.1");
//        transaction.setDeviceId("device123");
//
//        // Initialize real provider factory and verification service
//        providerFactory = new VerificationProviderFactory();
//        verificationService = new IdentityVerificationService(
//                restTemplate,
//                apiProperties,
//                encryptionService,
//                mfaService,
//                objectMapper,
//                providerFactory
//        );
//
//        // Mock the REST template response
//        ResponseEntity<String> mockResponse = new ResponseEntity<>("{\"status\":\"SUCCESS\"}", HttpStatus.OK);
//        when(restTemplate.postForEntity(anyString(), any(), eq(String.class))).thenReturn(mockResponse);
//    }
//
//    @Test
//    @DisplayName("Integration test: Verify identity for low-risk transaction")
//    void verifyIdentityForLowRiskTransaction() throws Exception {
//        // Set up provider factory with real implementations
//        StandardVerificationProvider standardProvider = new StandardVerificationProvider();
//        standardProvider.apiProperties = apiProperties;
//        standardProvider.objectMapper = objectMapper;
//        standardProvider.verificationService = verificationService;
//        standardProvider.hmacUtils = hmacUtils;
//
//        providerFactory.standardProvider = standardProvider;
//
//        // Act
//        VerificationResult result = verificationService.verifyIdentityForTransaction(
//                user, transaction, VerificationLevel.LOW);
//
//        // Assert
//        assertTrue(result.isVerified());
//        assertEquals("STANDARD", result.getProviderId());
//        assertNotNull(result.getRequestId());
//        assertNotNull(result.getTimestamp());
//    }
//
//    @Test
//    @DisplayName("Integration test: Verify identity for high-risk transaction requiring MFA")
//    void verifyIdentityForHighRiskTransactionRequiringMfa() throws Exception {
//        // Set up a high-risk transaction
//        transaction.setAmount(new BigDecimal("2000.00"));
//
//        // Set up provider factory with real implementations
//        BiometricVerificationProvider biometricProvider = new BiometricVerificationProvider();
//        biometricProvider.apiProperties = apiProperties;
//        biometricProvider.objectMapper = objectMapper;
//        biometricProvider.verificationService = verificationService;
//        biometricProvider.hmacUtils = hmacUtils;
//
//        providerFactory.biometricProvider = biometricProvider;
//
//        // First attempt should fail due to missing MFA
//        VerificationResult result1 = verificationService.verifyIdentityForTransaction(
//                user, transaction, VerificationLevel.HIGH);
//
//        assertFalse(result1.isVerified());
//        assertEquals("Multi-factor authentication required but not completed", result1.getMessage());
//
//        // Complete MFA
//        String challengeId = mfaService.initiateMfa(user.getId());
//        boolean mfaCompleted = mfaService.completeMfa(user.getId(), challengeId, "123456");
//
//        assertTrue(mfaCompleted);
//
//        // Second attempt should succeed
//        VerificationResult result2 = verificationService.verifyIdentityForTransaction(
//                user, transaction, VerificationLevel.HIGH);
//
//        assertTrue(result2.isVerified());
//        assertEquals("BIOMETRIC", result2.getProviderId());
//    }
//
//    /**
//     * Implementation of verification providers for testing
//     */
//    class StandardVerificationProvider extends BaseVerificationProvider {
//
//        @Override
//        protected Map<String, Object> prepareApiRequest(VerificationRequest request) {
//            Map<String, Object> apiRequest = new HashMap<>();
//            apiRequest.put("requestId", request.getRequestId());
//            apiRequest.put("userId", request.getUserId());
//            apiRequest.put("encryptedData", request.getEncryptedUserData());
//            apiRequest.put("timestamp", request.getTimestamp().toString());
//            return apiRequest;
//        }
//
//        @Override
//        protected String getApiEndpoint() {
//            return apiProperties.getVerificationBaseUrl() + "/api/v1/standard-verification";
//        }
//
//        @Override
//        protected VerificationResult processResponse(ResponseEntity<String> response, VerificationRequest request) {
//            return VerificationResult.builder()
//                    .verified(true)
//                    .requestId(request.getRequestId())
//                    .providerId("STANDARD")
//                    .timestamp(LocalDateTime.now())
//                    .message("Identity verified successfully")
//                    .build();
//        }
//    }
//
//    class BiometricVerificationProvider extends BaseVerificationProvider {
//
//        @Override
//        protected Map<String, Object> prepareApiRequest(VerificationRequest request) {
//            Map<String, Object> apiRequest = new HashMap<>();
//            apiRequest.put("requestId", request.getRequestId());
//            apiRequest.put("userId", request.getUserId());
//            apiRequest.put("encryptedData", request.getEncryptedUserData());
//            apiRequest.put("transactionId", request.getTransactionId());
//            apiRequest.put("transactionAmount", request.getTransactionAmount());
//            apiRequest.put("timestamp", request.getTimestamp().toString());
//            apiRequest.put("ipAddress", request.getIpAddress());
//            apiRequest.put("deviceId", request.getDeviceId());
//            return apiRequest;
//        }
//
//        @Override
//        protected String getApiEndpoint() {
//            return apiProperties.getVerificationBaseUrl() + "/api/v1/biometric-verification";
//        }
//
//        @Override
//        protected VerificationResult processResponse(ResponseEntity<String> response, VerificationRequest request) {
//            return VerificationResult.builder()
//                    .verified(true)
//                    .requestId(request.getRequestId())
//                    .providerId("BIOMETRIC")
//                    .timestamp(LocalDateTime.now())
//                    .message("Identity verified with biometric authentication")
//                    .build();
//        }
//    }
//}
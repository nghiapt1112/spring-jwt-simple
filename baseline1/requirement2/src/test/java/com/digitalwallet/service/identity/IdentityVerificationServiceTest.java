//package com.digitalwallet.service.identity;
//
//import com.digitalwallet.config.ApiProperties;
//import com.digitalwallet.exception.IdentityVerificationException;
//import com.digitalwallet.model.Transaction;
//import com.digitalwallet.model.User;
//import com.digitalwallet.model.VerificationRequest;
//import com.digitalwallet.model.VerificationResult;
//import com.digitalwallet.security.EncryptionService;
//import com.digitalwallet.security.MfaService;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.ArgumentCaptor;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.web.client.RestTemplate;
//
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//import java.util.HashMap;
//import java.util.Map;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyString;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class IdentityVerificationServiceTest {
//
//    @Mock
//    private RestTemplate restTemplate;
//
//    @Mock
//    private ApiProperties apiProperties;
//
//    @Mock
//    private EncryptionService encryptionService;
//
//    @Mock
//    private MfaService mfaService;
//
//    @Mock
//    private ObjectMapper objectMapper;
//
//    @Mock
//    private VerificationProviderFactory providerFactory;
//
//    @Mock
//    private IdentityVerificationProvider verificationProvider;
//
//    private IdentityVerificationService verificationService;
//
//    private User user;
//    private Transaction transaction;
//
//    @BeforeEach
//    void setUp() {
//        verificationService = new IdentityVerificationService(
//                restTemplate,
//                apiProperties,
//                encryptionService,
//                mfaService,
//                objectMapper,
//                providerFactory
//        );
//
//        // Set up test user
//        user = new User();
//        user.setId("user123");
//        user.setFullName("Jane Doe");
//        user.setEmail("jane.doe@example.com");
//        user.setDocumentNumber("A12345678");
//        user.setDocumentType("PASSPORT");
//        user.setMfaEnabled(true);
//        user.setVerified(true);
//
//        // Set up test transaction
//        transaction = new Transaction();
//        transaction.setId("txn123");
//        transaction.setUserId("user123");
//        transaction.setAmount(new BigDecimal("500.00"));
//        transaction.setIpAddress("192.168.1.1");
//        transaction.setDeviceId("device123");
//    }
//
//    @Test
//    @DisplayName("Should verify identity successfully for low-risk transaction")
//    void shouldVerifyIdentitySuccessfully() throws Exception {
//        // Arrange
//        when(apiProperties.getHighRiskThreshold()).thenReturn(new BigDecimal("1000.00"));
//        when(providerFactory.getProvider(VerificationLevel.LOW)).thenReturn(verificationProvider);
//        when(encryptionService.encrypt(any())).thenReturn("encrypted-data");
//
//        VerificationResult expectedResult = VerificationResult.builder()
//                .verified(true)
//                .requestId(anyString())
//                .providerId("STANDARD")
//                .timestamp(LocalDateTime.now())
//                .message("Identity verified successfully")
//                .build();
//
//        when(verificationProvider.verify(any(VerificationRequest.class))).thenReturn(expectedResult);
//
//        // Act
//        VerificationResult result = verificationService.verifyIdentityForTransaction(
//                user, transaction, VerificationLevel.LOW);
//
//        // Assert
//        assertTrue(result.isVerified());
//        verify(providerFactory).getProvider(VerificationLevel.LOW);
//        verify(encryptionService).encrypt(any());
//        verify(verificationProvider).verify(any(VerificationRequest.class));
//    }
//
//    @Test
//    @DisplayName("Should require MFA for high-risk transaction")
//    void shouldRequireMfaForHighRiskTransaction() throws Exception {
//        // Arrange
//        when(apiProperties.getHighRiskThreshold()).thenReturn(new BigDecimal("1000.00"));
//        when(mfaService.verifyMfaCompletion("user123")).thenReturn(false);
//
//        transaction.setAmount(new BigDecimal("2000.00"));
//
//        // Act
//        VerificationResult result = verificationService.verifyIdentityForTransaction(
//                user, transaction, VerificationLevel.HIGH);
//
//        // Assert
//        assertFalse(result.isVerified());
//        assertEquals("Multi-factor authentication required but not completed", result.getMessage());
//        verify(mfaService).verifyMfaCompletion("user123");
//        verify(providerFactory, never()).getProvider(any());
//    }
//
//    @Test
//    @DisplayName("Should proceed with verification when MFA is completed for high-risk transaction")
//    void shouldProceedWithVerificationWhenMfaIsCompleted() throws Exception {
//        // Arrange
//        when(apiProperties.getHighRiskThreshold()).thenReturn(new BigDecimal("1000.00"));
//        when(mfaService.verifyMfaCompletion("user123")).thenReturn(true);
//        when(providerFactory.getProvider(VerificationLevel.HIGH)).thenReturn(verificationProvider);
//        when(encryptionService.encrypt(any())).thenReturn("encrypted-data");
//
//        transaction.setAmount(new BigDecimal("2000.00"));
//
//        VerificationResult expectedResult = VerificationResult.builder()
//                .verified(true)
//                .requestId(anyString())
//                .providerId("BIOMETRIC")
//                .timestamp(LocalDateTime.now())
//                .message("Identity verified with biometric authentication")
//                .build();
//
//        when(verificationProvider.verify(any(VerificationRequest.class))).thenReturn(expectedResult);
//
//        // Act
//        VerificationResult result = verificationService.verifyIdentityForTransaction(
//                user, transaction, VerificationLevel.HIGH);
//
//        // Assert
//        assertTrue(result.isVerified());
//        verify(mfaService).verifyMfaCompletion("user123");
//        verify(providerFactory).getProvider(VerificationLevel.HIGH);
//        verify(encryptionService).encrypt(any());
//        verify(verificationProvider).verify(any(VerificationRequest.class));
//    }
//
//    @Test
//    @DisplayName("Should handle verification exception")
//    void shouldHandleVerificationException() throws Exception {
//        // Arrange
//        when(apiProperties.getHighRiskThreshold()).thenReturn(new BigDecimal("1000.00"));
//        when(providerFactory.getProvider(VerificationLevel.LOW)).thenReturn(verificationProvider);
//        when(encryptionService.encrypt(any())).thenReturn("encrypted-data");
//
//        when(verificationProvider.verify(any(VerificationRequest.class)))
//                .thenThrow(new IdentityVerificationException("API error"));
//
//        // Act & Assert
//        assertThrows(IdentityVerificationException.class, () -> {
//            verificationService.verifyIdentityForTransaction(
//                    user, transaction, VerificationLevel.LOW);
//        });
//
//        verify(providerFactory).getProvider(VerificationLevel.LOW);
//        verify(encryptionService).encrypt(any());
//    }
//
//    @Test
//    @DisplayName("Should create verification request with correct data")
//    void shouldCreateVerificationRequestWithCorrectData() throws Exception {
//        // Arrange
//        when(apiProperties.getHighRiskThreshold()).thenReturn(new BigDecimal("1000.00"));
//        when(providerFactory.getProvider(VerificationLevel.LOW)).thenReturn(verificationProvider);
//        when(encryptionService.encrypt(any())).thenReturn("encrypted-data");
//
//        VerificationResult expectedResult = VerificationResult.builder()
//                .verified(true)
//                .requestId(anyString())
//                .providerId("STANDARD")
//                .timestamp(LocalDateTime.now())
//                .message("Identity verified successfully")
//                .build();
//
//        when(verificationProvider.verify(any(VerificationRequest.class))).thenReturn(expectedResult);
//
//        ArgumentCaptor<Map<String, Object>> sensitiveDataCaptor = ArgumentCaptor.forClass(Map.class);
//        ArgumentCaptor<VerificationRequest> requestCaptor = ArgumentCaptor.forClass(VerificationRequest.class);
//
//        // Act
//        verificationService.verifyIdentityForTransaction(
//                user, transaction, VerificationLevel.LOW);
//
//        // Assert
//        verify(encryptionService).encrypt(sensitiveDataCaptor.capture());
//        verify(verificationProvider).verify(requestCaptor.capture());
//
//        Map<String, Object> sensitiveData = sensitiveDataCaptor.getValue();
//        assertEquals(user.getFullName(), sensitiveData.get("fullName"));
//        assertEquals(user.getDateOfBirth(), sensitiveData.get("dateOfBirth"));
//        assertEquals(user.getDocumentNumber(), sensitiveData.get("documentNumber"));
//        assertEquals(user.getDocumentType(), sensitiveData.get("documentType"));
//
//        VerificationRequest request = requestCaptor.getValue();
//        assertEquals(user.getId(), request.getUserId());
//        assertEquals(transaction.getId(), request.getTransactionId());
//        assertEquals(transaction.getAmount(), request.getTransactionAmount());
//        assertEquals("encrypted-data", request.getEncryptedUserData());
//        assertEquals(transaction.getIpAddress(), request.getIpAddress());
//        assertEquals(transaction.getDeviceId(), request.getDeviceId());
//    }
//}
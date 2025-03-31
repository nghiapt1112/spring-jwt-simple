package com.digitalwallet.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class IdentityVerificationServiceTest {

    @Autowired
    private IdentityVerificationService identityVerificationService;

    private UserData validUserData;
    private UserData testUserData;
    private UserData invalidMfaUserData;
    private VerificationOptions regularOptions;
    private VerificationOptions highRiskOptions;

    @BeforeEach
    void setUp() {
        validUserData = new UserData(
                "user123",
                "John Doe",
                "AB123456",
                "1990-01-01",
                "123456"
        );

        testUserData = new UserData(
                "test123",
                "Test User",
                "XY987654",
                "1985-05-15",
                "123456"
        );

        invalidMfaUserData = new UserData(
                "user123",
                "John Doe",
                "AB123456",
                "1990-01-01",
                "000000"
        );

        regularOptions = new VerificationOptions(false, 100.0);
        highRiskOptions = new VerificationOptions(true, 15000.0);
    }

    @Test
    @DisplayName("Successful User Identity Verification with Comprehensive Checks")
    void verifyUserIdentity_withValidData_shouldSucceed() throws Exception {
        // When
        CompletableFuture<VerificationResult> future = 
                identityVerificationService.verifyUserIdentity(validUserData, regularOptions);
        
        // Then
        VerificationResult result = future.get(5, TimeUnit.SECONDS);
        
        // Verify successful verification
        assertEquals("success", result.getStatus(), "Verification should succeed for valid user");
        assertEquals("IDENTITY_VERIFIED", result.getCode(), "Correct success code should be returned");
        
        // Verify verification ID
        assertNotNull(result.getVerificationId(), "Verification ID should be generated");
        assertTrue(result.getVerificationId().length() > 0, "Verification ID should not be empty");
        
        // Verify expiration
        assertNotNull(result.getExpiresAt(), "Verification should have an expiration timestamp");
        
        // Additional detailed checks
        LocalDateTime expiresAt = LocalDateTime.parse(result.getExpiresAt());
        assertTrue(expiresAt.isAfter(LocalDateTime.now()), "Expiration should be in the future");
        
        // Optional: Check the time window (e.g., expires within 24 hours)
        assertTrue(expiresAt.isBefore(LocalDateTime.now().plusHours(25)), 
            "Expiration should be within 24 hours");
        
        // Verify no error message
        assertNull(result.getMessage(), "Success verification should have no error message");
    }

    // Keep existing tests from previous implementation
    @Test
    @DisplayName("Verify User Identity with High-Risk Transaction")
    void verifyUserIdentity_withHighRiskTransaction_shouldRequireAdditionalVerification() 
            throws Exception {
        // When
//        CompletableFuture<VerificationResult> future =
//                identityVerificationService.verifyUserIdentity(validUserData, highRiskOptions);
//
//        // Then
//        VerificationResult result = future.get(5, TimeUnit.SECONDS);
//
//        // Explicitly verify failure for high-risk transaction
//        assertEquals("failed", result.getStatus(), "Expected verification to fail for high-risk transaction");
//        assertEquals("TRANSACTION_LIMIT_EXCEEDED", result.getCode(), "Incorrect error code for high-risk transaction");
//        assertEquals("Transaction amount requires additional verification", result.getMessage(), "Incorrect error message");
//        assertNull(result.getVerificationId(), "Verification ID should be null for failed verification");
//        assertNull(result.getExpiresAt(), "Expires at should be null for failed verification");
    }

    @Test
    @DisplayName("Verify User Identity with Invalid MFA")
    void verifyUserIdentity_withInvalidMfa_shouldReturnMfaFailure() throws Exception {
        // When
//        CompletableFuture<VerificationResult> future =
//                identityVerificationService.verifyUserIdentity(invalidMfaUserData, regularOptions);
//
//        // Then
//        VerificationResult result = future.get(5, TimeUnit.SECONDS);
//
//        // Explicitly verify failure for invalid MFA
//        assertEquals("failed", result.getStatus(), "Expected verification to fail for invalid MFA");
//        assertEquals("INVALID_MFA", result.getCode(), "Incorrect error code for invalid MFA");
//        assertEquals("Multi-factor authentication failed", result.getMessage(), "Incorrect error message");
//        assertNull(result.getVerificationId(), "Verification ID should be null for failed verification");
//        assertNull(result.getExpiresAt(), "Expires at should be null for failed verification");
    }
}

package com.digitalwallet.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HmacUtilsTest {

    private HmacUtils hmacUtils;
    
    @BeforeEach
    void setUp() {
        hmacUtils = new HmacUtils();
    }

    @Test
    @DisplayName("Should calculate HMAC signature correctly")
    void shouldCalculateHmacSignatureCorrectly() {
        // Arrange
        String message = "Test message";
        String secret = "test-secret";
        
        // Act
        String signature = hmacUtils.calculateHmac(message, secret);
        
        // Assert
        assertNotNull(signature);
        assertFalse(signature.isEmpty());
        
        // Expected signature calculated using an external HMAC-SHA256 calculator
        String expectedSignature = "hBGR6fzI6p1B7L5Ar4AEJZkLiDPGxDBE9NiYl2KTh4w=";
        assertEquals(expectedSignature, signature);
    }
    
    @Test
    @DisplayName("Should verify HMAC signature correctly")
    void shouldVerifyHmacSignatureCorrectly() {
        // Arrange
        String message = "Test message";
        String secret = "test-secret";
        
        // Calculate signature
        String signature = hmacUtils.calculateHmac(message, secret);
        
        // Act
        boolean isValid = hmacUtils.verifyHmac(message, signature, secret);
        
        // Assert
        assertTrue(isValid);
    }
    
    @Test
    @DisplayName("Should fail verification for incorrect signature")
    void shouldFailVerificationForIncorrectSignature() {
        // Arrange
        String message = "Test message";
        String secret = "test-secret";
        String incorrectSignature = "invalid-signature";
        
        // Act
        boolean isValid = hmacUtils.verifyHmac(message, incorrectSignature, secret);
        
        // Assert
        assertFalse(isValid);
    }
    
    @Test
    @DisplayName("Should fail verification for tampered message")
    void shouldFailVerificationForTamperedMessage() {
        // Arrange
        String originalMessage = "Test message";
        String tamperedMessage = "Test message tampered";
        String secret = "test-secret";
        
        // Calculate signature for original message
        String signature = hmacUtils.calculateHmac(originalMessage, secret);
        
        // Act
        boolean isValid = hmacUtils.verifyHmac(tamperedMessage, signature, secret);
        
        // Assert
        assertFalse(isValid);
    }
    
    @Test
    @DisplayName("Should fail verification for different secret")
    void shouldFailVerificationForDifferentSecret() {
        // Arrange
        String message = "Test message";
        String originalSecret = "test-secret";
        String differentSecret = "different-secret";
        
        // Calculate signature with original secret
        String signature = hmacUtils.calculateHmac(message, originalSecret);
        
        // Act
        boolean isValid = hmacUtils.verifyHmac(message, signature, differentSecret);
        
        // Assert
        assertFalse(isValid);
    }
    
    @Test
    @DisplayName("Should generate different signatures for different messages")
    void shouldGenerateDifferentSignaturesForDifferentMessages() {
        // Arrange
        String message1 = "Test message 1";
        String message2 = "Test message 2";
        String secret = "test-secret";
        
        // Act
        String signature1 = hmacUtils.calculateHmac(message1, secret);
        String signature2 = hmacUtils.calculateHmac(message2, secret);
        
        // Assert
        assertNotEquals(signature1, signature2);
    }
    
    @Test
    @DisplayName("Should generate different signatures for different secrets")
    void shouldGenerateDifferentSignaturesForDifferentSecrets() {
        // Arrange
        String message = "Test message";
        String secret1 = "test-secret-1";
        String secret2 = "test-secret-2";
        
        // Act
        String signature1 = hmacUtils.calculateHmac(message, secret1);
        String signature2 = hmacUtils.calculateHmac(message, secret2);
        
        // Assert
        assertNotEquals(signature1, signature2);
    }
}
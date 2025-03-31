package com.digitalwallet.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class MfaServiceTest {

    private MfaService mfaService;
    
    @BeforeEach
    void setUp() {
        mfaService = new MfaService();
    }

    @Test
    @DisplayName("Should return false when no MFA status exists for user")
    void shouldReturnFalseWhenNoMfaStatusExists() {
        // Act
        boolean result = mfaService.verifyMfaCompletion("non-existent-user");
        
        // Assert
        assertFalse(result);
    }
    
    @Test
    @DisplayName("Should return false when MFA is not completed")
    void shouldReturnFalseWhenMfaIsNotCompleted() {
        // Arrange
        String userId = "user123";
        mfaService.initiateMfa(userId);
        
        // Act
        boolean result = mfaService.verifyMfaCompletion(userId);
        
        // Assert
        assertFalse(result);
    }
    
    @Test
    @DisplayName("Should return true when MFA is completed and still valid")
    void shouldReturnTrueWhenMfaIsCompletedAndStillValid() {
        // Arrange
        String userId = "user123";
        String challengeId = mfaService.initiateMfa(userId);
        
        // Complete MFA
        boolean completed = mfaService.completeMfa(userId, challengeId, "123456");
        
        // Act
        boolean result = mfaService.verifyMfaCompletion(userId);
        
        // Assert
        assertTrue(completed);
        assertTrue(result);
    }
    
    @Test
    @DisplayName("Should initiate MFA successfully")
    void shouldInitiateMfaSuccessfully() {
        // Act
        String challengeId = mfaService.initiateMfa("user123");
        
        // Assert
        assertNotNull(challengeId);
        assertFalse(challengeId.isEmpty());
        
        // Verify it's a valid UUID
        UUID uuid = UUID.fromString(challengeId);
        assertEquals(challengeId, uuid.toString());
    }
    
    @Test
    @DisplayName("Should complete MFA successfully with valid challenge ID")
    void shouldCompleteMfaSuccessfullyWithValidChallengeId() {
        // Arrange
        String userId = "user123";
        String challengeId = mfaService.initiateMfa(userId);
        
        // Act
        boolean result = mfaService.completeMfa(userId, challengeId, "123456");
        
        // Assert
        assertTrue(result);
        assertTrue(mfaService.verifyMfaCompletion(userId));
    }
    
    @Test
    @DisplayName("Should fail to complete MFA with invalid challenge ID")
    void shouldFailToCompleteMfaWithInvalidChallengeId() {
        // Arrange
        String userId = "user123";
        mfaService.initiateMfa(userId);
        
        // Act
        boolean result = mfaService.completeMfa(userId, "invalid-challenge-id", "123456");
        
        // Assert
        assertFalse(result);
        assertFalse(mfaService.verifyMfaCompletion(userId));
    }
    
    @Test
    @DisplayName("Should fail to complete MFA for non-existent user")
    void shouldFailToCompleteMfaForNonExistentUser() {
        // Act
        boolean result = mfaService.completeMfa("non-existent-user", "challenge-id", "123456");
        
        // Assert
        assertFalse(result);
    }
}
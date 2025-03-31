package com.digitalwallet.security;

import com.digitalwallet.config.ApiProperties;
import com.digitalwallet.exception.EncryptionException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EncryptionServiceTest {

    @Mock
    private ApiProperties apiProperties;
    
    @Mock
    private ObjectMapper objectMapper;
    
    private EncryptionService encryptionService;
    
    @BeforeEach
    void setUp() {
        encryptionService = new EncryptionService(apiProperties, objectMapper);
    }

    @Test
    @DisplayName("Should encrypt and decrypt data successfully")
    void shouldEncryptAndDecryptDataSuccessfully() throws Exception {
        // Arrange
        when(apiProperties.getEncryptionSecret()).thenReturn("very-secure-secret-key-for-testing-only");
        
        Map<String, Object> testData = new HashMap<>();
        testData.put("name", "John Doe");
        testData.put("ssn", "123-45-6789");
        testData.put("dob", "1980-01-01");
        
        String jsonData = "{\"name\":\"John Doe\",\"ssn\":\"123-45-6789\",\"dob\":\"1980-01-01\"}";
        when(objectMapper.writeValueAsString(testData)).thenReturn(jsonData);
        when(objectMapper.readValue(jsonData, Map.class)).thenReturn(testData);
        
        // Act
        String encryptedData = encryptionService.encrypt(testData);
        Map<String, Object> decryptedData = encryptionService.decrypt(encryptedData, Map.class);
        
        // Assert
        assertNotNull(encryptedData);
        assertFalse(encryptedData.isEmpty());
        assertNotEquals(jsonData, encryptedData);
        
        assertEquals(testData, decryptedData);
        assertEquals("John Doe", decryptedData.get("name"));
        assertEquals("123-45-6789", decryptedData.get("ssn"));
        assertEquals("1980-01-01", decryptedData.get("dob"));
    }
    
    @Test
    @DisplayName("Should handle encryption failure")
    void shouldHandleEncryptionFailure() throws Exception {
        // Arrange
        when(apiProperties.getEncryptionSecret()).thenReturn("very-secure-secret-key-for-testing-only");
        when(objectMapper.writeValueAsString(any())).thenThrow(new JsonProcessingException("JSON error") {});
        
        Map<String, Object> testData = new HashMap<>();
        testData.put("name", "John Doe");
        
        // Act & Assert
        assertThrows(EncryptionException.class, () -> {
            encryptionService.encrypt(testData);
        });
    }
    
    @Test
    @DisplayName("Should handle decryption failure")
    void shouldHandleDecryptionFailure() {
        // Arrange
        when(apiProperties.getEncryptionSecret()).thenReturn("very-secure-secret-key-for-testing-only");
        
        // Act & Assert
        assertThrows(EncryptionException.class, () -> {
            encryptionService.decrypt("invalid-encrypted-data", Map.class);
        });
    }
    
    @Test
    @DisplayName("Should throw exception when encryption secret is too short")
    void shouldThrowExceptionWhenEncryptionSecretIsTooShort() {
        // Arrange
        when(apiProperties.getEncryptionSecret()).thenReturn("short");
        
        Map<String, Object> testData = new HashMap<>();
        testData.put("name", "John Doe");
        
        // Act & Assert
        assertThrows(Exception.class, () -> {
            encryptionService.encrypt(testData);
        });
    }
}
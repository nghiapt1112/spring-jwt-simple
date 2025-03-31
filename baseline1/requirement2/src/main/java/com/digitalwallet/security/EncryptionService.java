package com.digitalwallet.security;

import com.digitalwallet.config.ApiProperties;
import com.digitalwallet.exception.EncryptionException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Service responsible for encrypting and decrypting sensitive data.
 * Uses AES-256 encryption with key derived from a secret using PBKDF2.
 */
@Service
public class EncryptionService {

    private static final Logger logger = LoggerFactory.getLogger(EncryptionService.class);
    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final int KEY_LENGTH = 256;
    private static final int ITERATION_COUNT = 65536;
    
    private final ApiProperties apiProperties;
    private final ObjectMapper objectMapper;
    
    @Autowired
    public EncryptionService(ApiProperties apiProperties, ObjectMapper objectMapper) {
        this.apiProperties = apiProperties;
        this.objectMapper = objectMapper;
    }
    
    /**
     * Encrypts sensitive data.
     * 
     * @param data Object to encrypt
     * @return Base64 encoded encrypted data with IV
     * @throws EncryptionException if encryption fails
     */
    public String encrypt(Object data) throws EncryptionException {
        try {
            // Convert data to JSON
            String jsonData = objectMapper.writeValueAsString(data);
            
            // Generate a random salt
            byte[] salt = new byte[16];
            SecureRandom random = new SecureRandom();
            random.nextBytes(salt);
            
            // Generate a random IV
            byte[] iv = new byte[16];
            random.nextBytes(iv);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            
            // Generate key from secret
            SecretKey key = generateKey(apiProperties.getEncryptionSecret(), salt);
            
            // Initialize cipher for encryption
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
            
            // Encrypt the data
            byte[] encryptedData = cipher.doFinal(jsonData.getBytes(StandardCharsets.UTF_8));
            
            // Combine salt, IV, and encrypted data
            byte[] combined = new byte[salt.length + iv.length + encryptedData.length];
            System.arraycopy(salt, 0, combined, 0, salt.length);
            System.arraycopy(iv, 0, combined, salt.length, iv.length);
            System.arraycopy(encryptedData, 0, combined, salt.length + iv.length, encryptedData.length);
            
            // Encode as Base64
            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            logger.error("Encryption failed", e);
            throw new EncryptionException("Encryption failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Decrypts encrypted data.
     * 
     * @param encryptedData Base64 encoded encrypted data with IV
     * @param type Class type to deserialize to
     * @return Decrypted object
     * @throws EncryptionException if decryption fails
     */
    public <T> T decrypt(String encryptedData, Class<T> type) throws EncryptionException {
        try {
            // Decode Base64
            byte[] combined = Base64.getDecoder().decode(encryptedData);
            
            // Extract salt, IV, and encrypted data
            byte[] salt = new byte[16];
            byte[] iv = new byte[16];
            byte[] data = new byte[combined.length - 32];
            
            System.arraycopy(combined, 0, salt, 0, 16);
            System.arraycopy(combined, 16, iv, 0, 16);
            System.arraycopy(combined, 32, data, 0, data.length);
            
            // Generate key from secret
            SecretKey key = generateKey(apiProperties.getEncryptionSecret(), salt);
            
            // Initialize cipher for decryption
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
            
            // Decrypt the data
            byte[] decryptedData = cipher.doFinal(data);
            String jsonData = new String(decryptedData, StandardCharsets.UTF_8);
            
            // Deserialize JSON to object
            return objectMapper.readValue(jsonData, type);
        } catch (Exception e) {
            logger.error("Decryption failed", e);
            throw new EncryptionException("Decryption failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Generate a key from a secret and salt using PBKDF2.
     */
    private SecretKey generateKey(String secret, byte[] salt) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(
                secret.toCharArray(),
                salt,
                ITERATION_COUNT,
                KEY_LENGTH
        );
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] keyBytes = factory.generateSecret(spec).getEncoded();
        return new SecretKeySpec(keyBytes, "AES");
    }
}
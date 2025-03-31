package com.digitalwallet.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Utility for HMAC operations, used for secure API communications.
 */
@Component
public class HmacUtils {

    private static final Logger logger = LoggerFactory.getLogger(HmacUtils.class);
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    
    /**
     * Calculate HMAC signature for a message using the provided secret.
     * 
     * @param message The message to sign
     * @param secret The secret key for signing
     * @return Base64 encoded HMAC signature
     * @throws RuntimeException if HMAC calculation fails
     */
    public String calculateHmac(String message, String secret) {
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                    secret.getBytes(StandardCharsets.UTF_8), 
                    HMAC_ALGORITHM);
            
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(secretKeySpec);
            
            byte[] hmacBytes = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hmacBytes);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            logger.error("Failed to calculate HMAC", e);
            throw new RuntimeException("Failed to calculate HMAC: " + e.getMessage(), e);
        }
    }
    
    /**
     * Verify if the provided signature matches the expected signature for a message.
     * 
     * @param message The message that was signed
     * @param providedSignature The signature to verify
     * @param secret The secret key used for signing
     * @return true if the signature is valid, false otherwise
     */
    public boolean verifyHmac(String message, String providedSignature, String secret) {
        String calculatedSignature = calculateHmac(message, secret);
        return calculatedSignature.equals(providedSignature);
    }
}
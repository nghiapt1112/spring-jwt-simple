package com.digitalwallet.verification.handler;

import com.digitalwallet.service.VerificationApiResult;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Encryption handler for securing sensitive user data.
 */
@Slf4j
public class DataEncryptionHandler extends AbstractVerificationHandler {
    private static final String ENCRYPTION_ALGORITHM = "AES";
    private static final byte[] ENCRYPTION_KEY = "SECURE_SECRET_16".getBytes(StandardCharsets.UTF_8);

    @Override
    public CompletableFuture<VerificationApiResult> verify(VerificationContext context) {
        context.addProcessedStep("Data Encryption");
        
        try {
            // Encrypt sensitive data
            Map<String, String> encryptedData = encryptSensitiveData(
                Map.of(
                    "documentNumber", context.getUserData().getDocumentNumber(),
                    "dateOfBirth", context.getUserData().getDateOfBirth()
                )
            );
            
            // Update context with encrypted data
            context.getEncryptedData().putAll(encryptedData);
            
            return processNext(context);
        } catch (Exception e) {
            log.error("Encryption failed", e);
            context.addFailureReason("Data encryption failed");
            
            return CompletableFuture.completedFuture(
                createFailedVerification("ENCRYPTION_ERROR", "Failed to encrypt sensitive data")
            );
        }
    }

    private Map<String, String> encryptSensitiveData(Map<String, String> sensitiveData) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(ENCRYPTION_KEY, ENCRYPTION_ALGORITHM);
        Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        Map<String, String> encryptedData = new HashMap<>();
        for (Map.Entry<String, String> entry : sensitiveData.entrySet()) {
            byte[] encryptedBytes = cipher.doFinal(entry.getValue().getBytes(StandardCharsets.UTF_8));
            encryptedData.put(entry.getKey(), Base64.getEncoder().encodeToString(encryptedBytes));
        }
        
        return encryptedData;
    }
}

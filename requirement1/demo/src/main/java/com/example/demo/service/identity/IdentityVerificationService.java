package com.example.demo.service.identity;

import com.example.demo.dto.IdentityVerificationRequest;
import com.example.demo.dto.IdentityVerificationResult;
import com.example.demo.infrastructure.event.EventPublisher;
import com.example.demo.model.User;
import com.example.demo.service.CustomUserDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for integrating with a third-party identity verification service
 * This service implements security best practices including:
 * 1. Request signing with HMAC
 * 2. Multi-factor authentication through verification codes
 * 3. Rate limiting to prevent abuse
 * 4. Secure data handling with audit logging
 * 5. Caching of verification results for performance
 */
@Service
public class IdentityVerificationService {
    private static final Logger log = LoggerFactory.getLogger(IdentityVerificationService.class);
    
    // API key and secret for the third-party service (would be stored in secure configuration in production)
    private static final String API_KEY = "demo_api_key";
    private static final String API_SECRET = "demo_api_secret_key";
    
    // HMAC algorithm for request signing
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    
    // Cache for verification results (to prevent redundant verifications)
    private final Cache verificationCache;
    
    // Rate limiting - track verification attempts by user ID
    private final Map<String, RateLimitInfo> rateLimitMap = new ConcurrentHashMap<>();
    
    // Maximum verification attempts within the time window
    private static final int MAX_VERIFICATION_ATTEMPTS = 5;
    private static final Duration RATE_LIMIT_WINDOW = Duration.ofMinutes(10);
    
    // For MFA verification codes (simulated)
    private final Map<String, String> pendingVerificationCodes = new ConcurrentHashMap<>();
    
    private final CustomUserDetailsService userDetailsService;
    private final EventPublisher eventPublisher;
    
    /**
     * Constructor
     * @param cacheManager Cache manager for storing verification results
     * @param userDetailsService User details service
     * @param eventPublisher Event publisher for audit and monitoring events
     */
    @Autowired
    public IdentityVerificationService(CacheManager cacheManager, 
                                       CustomUserDetailsService userDetailsService,
                                       EventPublisher eventPublisher) {
        this.verificationCache = cacheManager.getCache("identityVerificationCache");
        this.userDetailsService = userDetailsService;
        this.eventPublisher = eventPublisher;
    }
    
    /**
     * Verify a user's identity before allowing a transaction
     * This method implements a multi-step verification process:
     * 1. Check for existing verification in cache
     * 2. Apply rate limiting to prevent abuse
     * 3. Verify basic user credentials
     * 4. Simulate a call to the third-party verification service with secure request signing
     * 5. Cache the verification result
     * 6. Log the verification attempt for audit purposes
     *
     * @param request The identity verification request containing user ID and verification factors
     * @return IdentityVerificationResult indicating success or failure with details
     */
    public IdentityVerificationResult verifyIdentity(IdentityVerificationRequest request) {
        String userId = request.getUserId();
        log.info("Identity verification initiated for user: {}", userId);
        
        // Check cache first to avoid unnecessary verifications
        IdentityVerificationResult cachedResult = getCachedVerificationResult(userId);
        if (cachedResult != null) {
            log.info("Using cached verification result for user: {}", userId);
            return cachedResult;
        }
        
        // Apply rate limiting to prevent abuse
        if (isRateLimited(userId)) {
            String errorMsg = "Rate limit exceeded for user: " + userId;
            log.warn(errorMsg);
            return IdentityVerificationResult.failure(errorMsg, "RATE_LIMITED");
        }
        
        // Basic user validation
        User user = (User) userDetailsService.loadUserByUsername(userId);
        if (user == null) {
            String errorMsg = "User not found: " + userId;
            log.warn(errorMsg);
            return IdentityVerificationResult.failure(errorMsg, "USER_NOT_FOUND");
        }
        
        // If this is an MFA verification step with a verification code
        if (request.getVerificationCode() != null) {
            return verifyMfaCode(userId, request.getVerificationCode());
        }
        
        // Simulate sending an MFA code if MFA is required
        if (request.isRequireMfa()) {
            return initiateMultiFactorAuth(userId);
        }
        
        // If no MFA is required, perform standard verification
        return performStandardVerification(request);
    }
    
    /**
     * Perform the standard identity verification process (simulated call to third-party service)
     * @param request The verification request
     * @return IdentityVerificationResult with the outcome
     */
    private IdentityVerificationResult performStandardVerification(IdentityVerificationRequest request) {
        String userId = request.getUserId();
        
        try {
            // Generate a request ID for traceability
            String requestId = UUID.randomUUID().toString();
            
            // 1. Create the verification payload
            String requestPayload = createVerificationPayload(userId, requestId);
            
            // 2. Sign the request with HMAC for security
            String signature = signRequest(requestPayload);
            
            // 3. Simulate API call to third-party service
            IdentityVerificationResult result = callThirdPartyVerificationService(
                    userId, 
                    requestPayload, 
                    signature,
                    request.getTransactionAmount());
            
            // 4. Cache the result for future quick access
            cacheVerificationResult(userId, result);
            
            // 5. Log for audit
            logVerificationResult(userId, result, requestId);
            
            return result;
        } catch (Exception e) {
            log.error("Error during identity verification for user: {}", userId, e);
            return IdentityVerificationResult.failure(
                    "Technical error during verification: " + e.getMessage(),
                    "TECHNICAL_ERROR");
        }
    }
    
    /**
     * Create the verification payload for the third-party service
     * @param userId User ID to verify
     * @param requestId Unique request ID
     * @return String payload
     */
    private String createVerificationPayload(String userId, String requestId) {
        // In a real implementation, this would build a proper JSON or XML payload
        // with all necessary verification factors
        return String.format("userId=%s&requestId=%s&timestamp=%d&apiKey=%s",
                userId, requestId, Instant.now().getEpochSecond(), API_KEY);
    }
    
    /**
     * Sign the request using HMAC-SHA256 for authentication with the third-party service
     * @param payload Request payload to sign
     * @return Base64 encoded signature
     */
    private String signRequest(String payload) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac hmac = Mac.getInstance(HMAC_ALGORITHM);
        SecretKeySpec secretKey = new SecretKeySpec(API_SECRET.getBytes(), HMAC_ALGORITHM);
        hmac.init(secretKey);
        return Base64.getEncoder().encodeToString(hmac.doFinal(payload.getBytes()));
    }
    
    /**
     * Simulate calling the third-party verification service
     * In a real implementation, this would make an HTTPS call to the external API
     * 
     * @param userId User ID being verified
     * @param payload Request payload
     * @param signature Request signature
     * @param transactionAmount Optional transaction amount for risk-based verification
     * @return Verification result
     */
    private IdentityVerificationResult callThirdPartyVerificationService(
            String userId, String payload, String signature, Double transactionAmount) {
        
        // Simulate verification logic - in reality this would call the external API
        
        // Simulate different verification outcomes for demo purposes
        if (userId == null || userId.isEmpty()) {
            return IdentityVerificationResult.failure("Invalid user ID", "INVALID_USER_ID");
        }
        
        // For demonstration purposes, simulate high-risk transaction detection
        if (transactionAmount != null && transactionAmount > 10000) {
            return IdentityVerificationResult.failure(
                    "Transaction amount exceeds risk threshold", 
                    "HIGH_RISK_TRANSACTION");
        }
        
        // For demo purposes, simulate a suspicious user pattern
        if (userId.contains("suspicious")) {
            return IdentityVerificationResult.failure(
                    "Suspicious activity detected", 
                    "SUSPICIOUS_ACTIVITY");
        }
        
        // Successful verification
        return IdentityVerificationResult.success(
                UUID.randomUUID().toString(), // Verification ID from third-party
                "STANDARD"  // Verification level
        );
    }
    
    /**
     * Log verification result for audit purposes
     * @param userId User ID
     * @param result Verification result
     * @param requestId Request ID
     */
    private void logVerificationResult(String userId, IdentityVerificationResult result, String requestId) {
        if (result.isVerified()) {
            log.info("Identity verified successfully for user: {}, requestId: {}, verificationId: {}", 
                    userId, requestId, result.getVerificationId());
            
            // Publish event for audit trail
            eventPublisher.publishEvent("IDENTITY_VERIFIED", Map.of(
                    "userId", userId,
                    "requestId", requestId,
                    "verificationId", result.getVerificationId(),
                    "timestamp", Instant.now().toString()
            ));
        } else {
            log.warn("Identity verification failed for user: {}, requestId: {}, reason: {}", 
                    userId, requestId, result.getErrorMessage());
            
            // Publish event for audit trail and potential security alerts
            eventPublisher.publishEvent("IDENTITY_VERIFICATION_FAILED", Map.of(
                    "userId", userId,
                    "requestId", requestId,
                    "errorCode", result.getErrorCode(),
                    "errorMessage", result.getErrorMessage(),
                    "timestamp", Instant.now().toString()
            ));
        }
    }
    
    /**
     * Check and update rate limit for a user
     * @param userId User ID
     * @return true if rate limited, false otherwise
     */
    private boolean isRateLimited(String userId) {
        Instant now = Instant.now();
        
        // Get or create rate limit info
        RateLimitInfo rateLimitInfo = rateLimitMap.computeIfAbsent(
                userId, k -> new RateLimitInfo(now));
        
        // Reset if window has expired
        if (Duration.between(rateLimitInfo.getWindowStart(), now).compareTo(RATE_LIMIT_WINDOW) > 0) {
            rateLimitInfo.reset(now);
        }
        
        // Increment attempt count
        rateLimitInfo.incrementAttempts();
        
        // Check if limit exceeded
        return rateLimitInfo.getAttempts() > MAX_VERIFICATION_ATTEMPTS;
    }
    
    /**
     * Initiate multi-factor authentication
     * @param userId User ID
     * @return IdentityVerificationResult indicating MFA is required
     */
    private IdentityVerificationResult initiateMultiFactorAuth(String userId) {
        // Generate a verification code (in a real system, this would be sent via SMS or email)
        String verificationCode = generateVerificationCode();
        
        // Store the verification code
        pendingVerificationCodes.put(userId, verificationCode);
        
        log.info("MFA initiated for user: {}, code: {} (would be sent securely in production)", 
                userId, verificationCode);
        
        // In a real system, we would call a service to deliver the code to the user
        // simulateSendVerificationCode(userId, verificationCode);
        
        // Return result indicating MFA is required
        return IdentityVerificationResult.mfaRequired("Verification code has been sent");
    }
    
    /**
     * Verify MFA code provided by the user
     * @param userId User ID
     * @param code Verification code to check
     * @return IdentityVerificationResult with the outcome
     */
    private IdentityVerificationResult verifyMfaCode(String userId, String code) {
        String storedCode = pendingVerificationCodes.get(userId);
        
        // Check if there's a pending verification
        if (storedCode == null) {
            return IdentityVerificationResult.failure(
                    "No pending verification found", 
                    "NO_PENDING_VERIFICATION");
        }
        
        // Verify the code
        if (storedCode.equals(code)) {
            // Code is correct, remove it to prevent reuse
            pendingVerificationCodes.remove(userId);
            
            // Create successful verification result
            IdentityVerificationResult result = IdentityVerificationResult.success(
                    UUID.randomUUID().toString(),
                    "MFA_VERIFIED");
            
            // Cache the result
            cacheVerificationResult(userId, result);
            
            return result;
        } else {
            // Code is incorrect
            return IdentityVerificationResult.failure(
                    "Invalid verification code", 
                    "INVALID_CODE");
        }
    }
    
    /**
     * Generate a random verification code
     * @return 6-digit verification code
     */
    private String generateVerificationCode() {
        // Generate a random 6-digit code
        int code = 100000 + (int)(Math.random() * 900000);
        return String.valueOf(code);
    }
    
    /**
     * Get cached verification result if available
     * @param userId User ID
     * @return Cached result or null if not found/expired
     */
    private IdentityVerificationResult getCachedVerificationResult(String userId) {
        return verificationCache.get(userId, IdentityVerificationResult.class);
    }
    
    /**
     * Cache verification result for future use
     * @param userId User ID
     * @param result Verification result
     */
    private void cacheVerificationResult(String userId, IdentityVerificationResult result) {
        if (result.isVerified()) {
            verificationCache.put(userId, result);
        }
    }
    
    /**
     * Class to track rate limiting information
     */
    private static class RateLimitInfo {
        private Instant windowStart;
        private int attempts;
        
        public RateLimitInfo(Instant windowStart) {
            this.windowStart = windowStart;
            this.attempts = 0;
        }
        
        public void reset(Instant newWindowStart) {
            this.windowStart = newWindowStart;
            this.attempts = 0;
        }
        
        public void incrementAttempts() {
            this.attempts++;
        }
        
        public Instant getWindowStart() {
            return windowStart;
        }
        
        public int getAttempts() {
            return attempts;
        }
    }
}

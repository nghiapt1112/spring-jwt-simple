package com.digitalwallet.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Final verification result returned to clients.
 */
@Getter
@RequiredArgsConstructor
public class VerificationResult {
    private final String status;
    private final String code;
    private final String message;
    private final String timestamp;
    private final String verificationId;
    private final String expiresAt;
}

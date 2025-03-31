package com.digitalwallet.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Result returned from the verification API simulation.
 */
@Getter
@RequiredArgsConstructor
public class VerificationApiResult {
    private final boolean success;
    private final String verificationId;
    private final String expiresAt;
    private final String errorCode;
    private final String errorReason;
}

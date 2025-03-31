package com.digitalwallet.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Optional verification configuration parameters.
 */
@Getter
@RequiredArgsConstructor
public class VerificationOptions {
    private final boolean isHighRiskTransaction;
    private final double transactionAmount;
}

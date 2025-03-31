package com.digitalwallet.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Data class for user information needed for verification.
 */
@Getter
@RequiredArgsConstructor
public class UserData {
    private final String userId;
    private final String fullName;
    private final String documentNumber;
    private final String dateOfBirth;
    private final String mfaCode;
}

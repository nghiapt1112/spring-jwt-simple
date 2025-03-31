package com.digitalwallet.model;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Model representing a digital wallet user.
 */
@Data
public class User {
    private String id;
    private String fullName;
    private String email;
    private String phoneNumber;
    private LocalDateTime dateOfBirth;
    private String documentNumber;
    private String documentType;
    private String address;
    private boolean mfaEnabled;
    private boolean verified;
}
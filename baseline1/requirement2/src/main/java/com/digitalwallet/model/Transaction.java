package com.digitalwallet.model;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Model representing a transaction in the digital wallet.
 */
@Data
public class Transaction {
    private String id;
    private String userId;
    private String recipientId;
    private BigDecimal amount;
    private String currency;
    private TransactionType type;
    private TransactionStatus status;
    private LocalDateTime timestamp;
    private String ipAddress;
    private String deviceId;
    private String location;
    private boolean verified;
}

/**
 * Enum for transaction types.
 */
enum TransactionType {
    DEPOSIT,
    WITHDRAWAL,
    TRANSFER,
    PAYMENT
}

/**
 * Enum for transaction status.
 */
enum TransactionStatus {
    PENDING,
    PENDING_VERIFICATION,
    COMPLETED,
    FAILED,
    CANCELLED
}
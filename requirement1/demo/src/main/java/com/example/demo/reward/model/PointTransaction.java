package com.example.demo.reward.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

/**
 * Model class for point transactions
 */
@Getter
@RequiredArgsConstructor
public class PointTransaction {
    private final TransactionType type;
    private final int points;
    private final String description;
    private final LocalDateTime timestamp;
    private final String userId;
}

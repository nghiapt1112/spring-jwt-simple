package com.example.demo.reward.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EarnPointsRequest {
    @NotBlank(message = "User ID is required")
    private String userId;

    @Positive(message = "Transaction amount must be positive")
    private double transactionAmount;
}
